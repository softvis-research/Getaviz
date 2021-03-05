var relationController = function () {

	//var sourceEntities = new Array();
	var sourceEntities = new Array();

	var relatedEntitiesMap = new Map();
	var relatedEntitiesSet = new Set();

	var connectors = new Array();
	var relations = new Array();

	var activated = false;

	var faded = false;

	//config parameters	
	var controllerConfig = {
		showConnector: true,
		showHighlight: true,
		showTransparency: false,

		showRecursiveRelations: true,
		useMultiSelect: true,

		//connector configs
		fixPositionY: false,
		fixPositionZ: false,
		showInnerRelations: false,
		sourceStartAtParentBorder: false,
		targetEndAtParentBorder: false,
		sourceStartAtBorder: false,
		targetEndAtBorder: false,
		createEndpoints: false,
		connectorColor: { r: 1, g: 0, b: 0 },
		endpointColor: { r: 0, g: 0, b: 0 },

		//highlight configs
		highlightColor: "black",
		unfadeOnHighlight: false,

		//transparency configs
		fullFadeValue: 0.55,
		halfFadeValue: 0.55,
		noFadeValue: 0,
		startFaded: false
	}


	function initialize(setupConfig) {

		application.transferConfigParams(setupConfig, controllerConfig);

		events.selected.on.subscribe(onRelationsChanged);

		events.selected.off.subscribe(reset);
	}

	function activate() {
		activated = true;
		if (relatedEntitiesMap.size != 0) {
			if (controllerConfig.showConnector) {
				createRelatedConnections();
			}
			if (controllerConfig.showHighlight) {
				highlightRelatedEntities();
			}
			if (controllerConfig.showTransparency) {
				if (controllerConfig.startFaded) {
					setTimeout(fadeAllEntities, 1000);
				}
				fadeNotRelatedEntities();
			}
		}
	}

	function deactivate() {
		reset();
		activated = false;
	}

	function reset() {

		if (controllerConfig.showConnector) {
			removeAllConnectors();
		}
		if (controllerConfig.showHighlight) {
			unhighlightRelatedEntities();
		}
		if (controllerConfig.showTransparency) {
			if (faded) {
				setTimeout(unfadeAllEntities, 1000);
			}
			faded = false;
		}

		//remove relation entities
		relations.forEach(function (relation) {
			model.removeEntity(relation.id);
		});

		sourceEntities = new Array();
		relatedEntitiesMap = new Map();
		relatedEntitiesSet = new Set();
		relations = new Array();
	}



	function onRelationsChanged(applicationEvent) {

		events.log.info.publish({ text: "connector - onRelationsChanged" });

		//get related entities
		if (controllerConfig.useMultiSelect) {
			sourceEntities = applicationEvent.entities;
		} else {
			sourceEntities.push(applicationEvent.entities[0]);
		}

		events.log.info.publish({ text: "connector - onRelationsChanged - selected Entity - " + applicationEvent.entities[0] });

		getRelatedEntities(sourceEntities);

		if (controllerConfig.showRecursiveRelations) {
			getRecursiveRelations(sourceEntities);
		}

		events.log.info.publish({ text: "connector - onRelationsChanged - related Entities - " + relatedEntitiesMap.size });

		if (relatedEntitiesMap.size == 0) {
			return;
		}

		if (activated) {
			if (controllerConfig.showConnector) {
				createRelatedConnections();
			}
			if (controllerConfig.showHighlight) {
				highlightRelatedEntities();
			}
			if (controllerConfig.showTransparency) {
				fadeNotRelatedEntities();
			}
		}

	}

	function getRelatedEntities(sourceEntitiesArray) {
		sourceEntitiesArray.forEach(function (sourceEntity) {
			if (relatedEntitiesMap.has(sourceEntity)) {
				//sourceEntity already analyzed
				return;
			}

			var allRelatedEntitiesOfSourceEntity = getRelatedEntitiesOfSourceEntity(sourceEntity);
			var relatedEntitiesOfSourceEntity = new Array();

			allRelatedEntitiesOfSourceEntity.forEach(function (relatedEntity) {
				if (relatedEntitiesOfSourceEntity.includes(relatedEntity)) {
					events.log.info.publish({ text: "connector - onRelationsChanged - multiple relation" });
					return;
				}

				if (controllerConfig.showInnerRelations === false) {
					if (isTargetChildOfSourceParent(relatedEntity, sourceEntity)) {
						events.log.info.publish({ text: "connector - onRelationsChanged - inner relation" });
						return;
					}
				}

				if (canvasManipulator.elementIsHidden(relatedEntity.id)) {
					events.log.info.publish({ text: "connector - onRelationsChanged - element hidden" });
					return;
				}

				//create model entity
				var relation = model.createEntity(
					"Relation",
					sourceEntity.id + "--2--" + relatedEntity.id,
					sourceEntity.name + " - " + relatedEntity.name,
					sourceEntity.name + " - " + relatedEntity.name,
					sourceEntity
				);

				relation.source = sourceEntity;
				relation.target = relatedEntity;

				relations.push(relation);

				relatedEntitiesOfSourceEntity.push(relatedEntity);
				relatedEntitiesSet.add(relatedEntity);
			})

			relatedEntitiesMap.set(sourceEntity, relatedEntitiesOfSourceEntity);
		});
	}

	function getRelatedEntitiesOfSourceEntity(sourceEntity) {
		var relatedEntitiesOfSourceEntity = new Array();

		switch (sourceEntity.type) {
			case "Class":
			case "Interface":
				//relatedEntitiesOfSourceEntity = relatedEntitiesOfSourceEntity.concat(sourceEntity.superTypes);
				//relatedEntitiesOfSourceEntity = relatedEntitiesOfSourceEntity.concat(sourceEntity.subTypes);
				break;
			case "ParameterizableClass":
				relatedEntitiesOfSourceEntity = relatedEntitiesOfSourceEntity.concat(sourceEntity.superTypes);
				//relatedEntitiesOfSourceEntity = relatedEntitiesOfSourceEntity.concat(sourceEntity.subTypes);
				break;
			case "Attribute":
				relatedEntitiesOfSourceEntity = sourceEntity.accessedBy;
				break;
			case "Method":
			case "Function":
				relatedEntitiesOfSourceEntity = sourceEntity.accesses;
			case "FunctionModule":
			case "Report":
			case "FormRoutine":
				relatedEntitiesOfSourceEntity = relatedEntitiesOfSourceEntity.concat(sourceEntity.calls);
				//relatedEntitiesOfSourceEntity = relatedEntitiesOfSourceEntity.concat(sourceEntity.calledBy);
				break;
			case "Reference":
				relatedEntitiesOfSourceEntity = relatedEntitiesOfSourceEntity.concat(sourceEntity.rcData);
				break;
		}

		return relatedEntitiesOfSourceEntity;
	}

	function getRecursiveRelations(oldSourceEntities) {
		oldSourceEntities.forEach(function (oldSourceEntity) {
			var relatedEntities = relatedEntitiesMap.get(oldSourceEntity);

			if (relatedEntities.length == 0) {
				return;
			}

			newSourceEntities = relatedEntities.filter(relatedEntity => (!relatedEntitiesMap.has(relatedEntity)));

			if (newSourceEntities.length == 0) {
				return;
			}

			getRelatedEntities(newSourceEntities);
			getRecursiveRelations(newSourceEntities);
		});
	}


	/*************************
			Connection
	*************************/

	function createRelatedConnections() {

		relations.forEach(function (relation) {
			var sourceEntity = relation.source;
			var relatedEntity = relation.target;

			//create scene element
			let connectorElements = createConnector(sourceEntity, relatedEntity);

			//source or target not rendered -> no connector
			if (connectorElements === undefined) {
				events.log.error.publish({ text: "connector - createRelatedConnections - source or target not rendered" });
				return;
			}

			events.log.info.publish({ text: "connector - createRelatedConnections - create connector" });

			connectorElements.forEach(function (element) {
				connectors.push(element);
			});
		})
	}

	function createConnector(entity, relatedEntity) {

		//calculate attributes						
		var sourcePosition = canvasManipulator.getCenterOfEntity(entity);
		if (sourcePosition === null) {
			return;
		}

		var targetPosition = canvasManipulator.getCenterOfEntity(relatedEntity);
		if (targetPosition === null) {
			return;
		}

		if (controllerConfig.sourceStartAtParentBorder) {
			let sourceParent = entity.belongsTo;
			let targetParent = relatedEntity.belongsTo;
			if (sourceParent != targetParent) {
				if (controllerConfig.targetEndAtParentBorder) {
					targetPosition = canvasManipulator.getCenterOfEntity(targetParent);
				}
				let intersection = calculateBorderPosition(targetPosition, canvasManipulator.getCenterOfEntity(sourceParent), sourceParent);
				if (intersection != undefined) {
					sourcePosition = intersection;
				} else console.debug("raycasting found no intersection with parent objects surface");
			}
		}

		if (controllerConfig.targetEndAtParentBorder) {
			let targetParent = relatedEntity.belongsTo;
			if (targetParent != entity.belongsTo) {
				let intersection = calculateBorderPosition(sourcePosition, canvasManipulator.getCenterOfEntity(targetParent), targetParent);
				if (intersection != undefined) {
					targetPostion = intersection;
				} else console.debug("raycasting found no intersection with parent objects surface");
			}
		}

		if (controllerConfig.sourceStartAtBorder) {
			if (controllerConfig.targetEndAtBorder) {
				targetPosition = canvasManipulator.getCenterOfEntity(relatedEntity);
			}
			// getCenterOfEntity again in-case it got overwritten for sourceStartAtParentBorder
			sourcePosition = calculateBorderPosition(targetPosition, canvasManipulator.getCenterOfEntity(entity), entity);
		}
		if (controllerConfig.targetEndAtBorder) {
			// getCenterOfEntity again in-case it got overwritten for targetEndAtParentBorder
			targetPosition = calculateBorderPosition(sourcePosition, canvasManipulator.getCenterOfEntity(relatedEntity), relatedEntity);
		}

		var connectorSize = 0.05;

		// This function made no sense and doesn't seem to work on x3dom either
		/*if(controllerConfig.fixPositionZ) {
			sourcePosition.z = controllerConfig.fixPositionZ;
			targetPosition.z = controllerConfig.fixPositionZ;
		}*/
		// suggestion for city model: draw horizontal cylinders on the lower positions level
		if (controllerConfig.fixPositionY) {
			sourcePosition.y = Math.min(sourcePosition.y, targetPosition.y);
			targetPosition.y = sourcePosition.y;
		}

		let deltaX = targetPosition.x - sourcePosition.x;
		let deltaY = targetPosition.y - sourcePosition.y;
		let deltaZ = targetPosition.z - sourcePosition.z;

		let distance = sourcePosition.distanceTo(targetPosition);
		let direction = new THREE.Vector3(deltaX, deltaY, deltaZ).normalize();

		//create connector
		var connector = document.createElement("a-cylinder");

		connector.addEventListener("loaded", function () {
			let threeMesh = this.object3DMap.mesh;

			threeMesh.scale.set(connectorSize, distance, connectorSize);
			threeMesh.material.color.setRGB(controllerConfig.connectorColor.r, controllerConfig.connectorColor.g, controllerConfig.connectorColor.b);
			threeMesh.position.set(sourcePosition.x + deltaX / 2,
				sourcePosition.y + deltaY / 2,
				sourcePosition.z + deltaZ / 2);

			connector.setAttribute("radius", 5);



			var quaternion = threeMesh.quaternion;
			quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), direction);

		});
		connector.setAttribute("flat-shading", true);
		connector.setAttribute("shader", "flat");
		//                 connector.setAttribute("radius", 5);



		let scene = document.querySelector("a-scene");
		scene.appendChild(connector);
		var connectorElements = [];
		connectorElements.push(connector);

		// create Endpoints
		if (controllerConfig.createEndpoints) {
			var size = connectorSize * 1.5;
			var length = size * 6;
			var sourceEndpoint = document.createElement("a-cylinder");
			sourceEndpoint.addEventListener("loaded", function () {
				let threeMesh = this.object3DMap.mesh;
				threeMesh.material.color.setRGB(controllerConfig.endpointColor);
				threeMesh.scale.set(size, length, size);
				threeMesh.position.set(sourcePosition.x, sourcePosition.y, sourcePosition.z);
				var quaternion = threeMesh.quaternion;
				quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), direction);
			});
			sourceEndpoint.setAttribute("flat-shading", true);
			sourceEndpoint.setAttribute("shader", "flat");

			var targetEndpoint = document.createElement("a-cylinder");
			targetEndpoint.addEventListener("loaded", function () {
				let threeMesh = this.object3DMap.mesh;
				threeMesh.material.color.setRGB(controllerConfig.endpointColor);
				threeMesh.scale.set(size, length, size);
				threeMesh.position.set(targetPosition.x, targetPosition.y, targetPosition.z);
				var quaternion = threeMesh.quaternion;
				quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), direction.normalize());
			});
			targetEndpoint.setAttribute("shader", "flat");

			scene.appendChild(sourceEndpoint);
			scene.appendChild(targetEndpoint);
			connectorElements.push(sourceEndpoint);
			connectorElements.push(targetEndpoint);

		}
		return connectorElements;
	}

	function removeAllConnectors() {

		events.log.info.publish({ text: "connector - removeAllConnectors" });

		if (connectors.length == 0) {
			return;
		}

		//remove scene elements
		connectors.forEach(function (connector) {
			canvasManipulator.removeElement(connector);
		});

		connectors = new Array();
	}

	function calculateBorderPosition(sourceOfRay, targetOfRay, entity) {
		let object = document.getElementById(entity.id);
		let raycaster = new THREE.Raycaster();
		raycaster.set(sourceOfRay, targetOfRay.subVectors(targetOfRay, sourceOfRay).normalize());
		let intersection = raycaster.intersectObject(object.object3DMap.mesh);
		return intersection[0].point;
	}


	/************************
			Highlight
	************************/

	function highlightRelatedEntities() {
		if (relatedEntitiesSet.size == 0) {
			return;
		}

		if (controllerConfig.unfadeOnHighlight) {
			canvasManipulator.resetTransparencyOfEntities(Array.from(relatedEntitiesSet.values()).filter(relatedEntity => !(relatedEntity.marked)), { name: "relationController" });
		}

		canvasManipulator.highlightEntities(Array.from(relatedEntitiesSet.values()), controllerConfig.highlightColor, { name: "relationController" });
	}

	function unhighlightRelatedEntities() {
		if (relatedEntitiesSet.size == 0) {
			return;
		}

		canvasManipulator.unhighlightEntities(Array.from(relatedEntitiesSet.values()), { name: "relationController" });
	}


	/***************************
			Transparency
	***************************/

	function fadeNotRelatedEntities() {

		fadeAllEntities();


		//unfade related entities
		canvasManipulator.changeTransparencyOfEntities(relatedEntitiesSet, controllerConfig.noFadeValue, { name: "relationController" });

		// //unfade parents of related entities				
		// canvasManipulator.changeTransparencyOfEntities(parents, controllerConfig.halfFadeValue, { name: "relationController" });
	}

	function fadeAllEntities() {
		if (!faded) {
			//really really bad fix for one model where elements in scene but not in model...
			//add an all elements functionality for canvasmanipulator anyway 
			var allCanvasElementIDs = canvasManipulator.getElementIds();
			var allCanvasObjects = [];
			allCanvasElementIDs.filter(canvasElementID => canvasElementID != "").forEach(canvasElementID => allCanvasObjects.push({ id: canvasElementID }));

			canvasManipulator.changeTransparencyOfEntities(allCanvasObjects, controllerConfig.fullFadeValue, { name: "relationController" });
			faded = true;
		}
	}

	function unfadeAllEntities() {

		//really really bad fix for one model where elements in scene but not in model...
		//add an all elements functionality for canvasmanipulator anyway 
		var allCanvasElementIDs = canvasManipulator.getElementIds();
		var allCanvasObjects = [];
		allCanvasElementIDs.filter(canvasElementID => canvasElementID != "").forEach(canvasElementID => allCanvasObjects.push({ id: canvasElementID }));

		canvasManipulator.changeTransparencyOfEntities(allCanvasObjects, controllerConfig.noFadeValue, { name: "relationController" });

	}


	function isTargetChildOfSourceParent(target, source) {

		var targetParent = target.belongsTo;
		var sourceParent = source.belongsTo;

		while (targetParent !== undefined) {

			if (targetParent == sourceParent) {
				return true;
			}

			targetParent = targetParent.belongsTo;
		}

		return false;
	}


	return {
		initialize: initialize,
		reset: reset,
		activate: activate,
		deactivate: deactivate
	};

}();
