var relationController = function () {

	// list of entities whose relations to others are being displayed (not including intermediate steps of recursive relations)
	var sourceEntities = new Array();
	// for every source entity (and intermediate of recursive relations), the list of entities it is related to
	var relatedEntitiesMap = new Map();
	// set of all entities that are related to sourceEntities overall, not including the source entities themselves
	var relatedEntitiesSet = new Set();

	var connectors = new Array();
	var relations = new Array();

	var activated = false;

	//config parameters
	var controllerConfig = {
		showConnector: true,
		showHighlight: true,

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
	}


	function initialize(setupConfig) {

		application.transferConfigParams(setupConfig, controllerConfig);

		events.selected.on.subscribe(onRelationsChanged);
		events.selected.off.subscribe(onEntityDeselected);
		events.filtered.on.subscribe(onEntityFiltered);
	}

	function activate() {
		activated = true;
		unhideRelatedEntities().then(() => {
			if (relatedEntitiesMap.size != 0) {
				if (controllerConfig.showConnector) {
					createRelatedConnections();
				}
				if (controllerConfig.showHighlight) {
					highlightRelatedEntities();
				}
			}
		});
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

		//remove relation entities
		relations.forEach(function (relation) {
			model.removeEntity(relation.id);
		});

		sourceEntities = new Array();
		relatedEntitiesMap = new Map();
		relatedEntitiesSet = new Set();
		relations = new Array();
	}

	function foreachEntityInRelationTree(relationMap, rootEntity, operationFunction, alreadyVisitedSet) {
		operationFunction(rootEntity);
		if (relationMap.has(rootEntity)) {
			for (const relatedEntity of relationMap.get(rootEntity)) {
				// guard against cycles (and therefore infinite recursion) in the relation tree
				if (!alreadyVisitedSet.has(relatedEntity)) {
					alreadyVisitedSet.add(relatedEntity);
					foreachEntityInRelationTree(relationMap, relatedEntity, operationFunction, alreadyVisitedSet);
				}
			}
		}
	}

	function onEntityDeselected(applicationEvent) {
		const deselectedEntities = new Set(applicationEvent.entities);
		// all source entities were deselected
		if (sourceEntities.every(entity => deselectedEntities.has(entity))) {
			reset();
			return;
		}
		// there is currently no way to deselect only a subset without filtering it
	}

	function onEntityFiltered(applicationEvent) {
		const entities = applicationEvent.entities;

		if (entities.every(entity => !relatedEntitiesSet.has(entity) && !sourceEntities.includes(entity))) {
			return;
		}

		const filteredEntities = new Set(entities);
		// all source entities were filtered
		if (sourceEntities.every(entity => filteredEntities.has(entity))) {
			reset();
			return;
		}

		// we need to remove relations to filtered entities from the map before doing the recursive passes over the tree
		// otherwise the second pass will invalidate the first pass if a filtered element is the relation target of a source element
		for (const [source, targets] of relatedEntitiesMap) {
			if (targets.length) {
				relatedEntitiesMap.set(source, targets.filter(entity => !filteredEntities.has(entity)));
			}
		}

		const relatedEntitiesToRemove = new Set();
		for (const entity of filteredEntities) {
			foreachEntityInRelationTree(relatedEntitiesMap, entity,	(entity) => relatedEntitiesToRemove.add(entity), new Set());
		}
		// there can be multiple relation paths to the same element - keep anything that still has a valid path to it
		const remainingSourceEntities = sourceEntities.filter(entity => !filteredEntities.has(entity));
		for (const entity of remainingSourceEntities) {
			foreachEntityInRelationTree(relatedEntitiesMap, entity, (entity) => relatedEntitiesToRemove.delete(entity), new Set());
		}

		sourceEntities = remainingSourceEntities;

		removeRelationsToAndFrom(relatedEntitiesToRemove);

		// clean up entities that are no longer targeted, but remain sources (and thus couldn't have their outgoing relations removed)
		const remainingRelationTargets = new Set(relations.map(relation => relation.target));
		const previouslyRelatedSourceEntities =
			sourceEntities.filter(entity => relatedEntitiesSet.has(entity) && !remainingRelationTargets.has(entity));
		for (const entity of previouslyRelatedSourceEntities) {
			relatedEntitiesSet.delete(entity);
		}
		canvasManipulator.unhighlightEntities(previouslyRelatedSourceEntities, { name: "relationController" });
	}

	// does not remove these entities from the target lists of relatedEntitiesMap
	function removeRelationsToAndFrom(entitySet) {
		for (const relatedEntity of entitySet) {
			relatedEntitiesSet.delete(relatedEntity);
			relatedEntitiesMap.delete(relatedEntity);
		}

		const remainingRelations = [];
		const connectorsToDelete = new Set();
		for (const relation of relations) {
			if (entitySet.has(relation.target) || entitySet.has(relation.source)) {
				model.removeEntity(relation.id);
				connectorsToDelete.add(relation.id);
			} else {
				remainingRelations.push(relation);
			}
		}
		relations = remainingRelations;

		const remainingConnectors = [];
		for (const connector of connectors) {
			if (connectorsToDelete.has(connector.getAttribute("id"))) {
				canvasManipulator.removeElement(connector);
			} else {
				remainingConnectors.push(connector);
			}
		}
		connectors = remainingConnectors;

		canvasManipulator.unhighlightEntities([...entitySet], { name: "relationController" });
	}

	async function onRelationsChanged(applicationEvent) {

		events.log.info.publish({ text: "connector - onRelationsChanged" });

		//get related entities
		if (controllerConfig.useMultiSelect) {
			sourceEntities = applicationEvent.entities;
		} else {
			sourceEntities.push(applicationEvent.entities[0]);
		}

		events.log.info.publish({ text: "connector - onRelationsChanged - selected Entity - " + applicationEvent.entities[0] });

		await getRelatedEntities(sourceEntities);

		if (controllerConfig.showRecursiveRelations) {
			await getRecursiveRelations(sourceEntities);
		}

		events.log.info.publish({ text: "connector - onRelationsChanged - related Entities - " + relatedEntitiesMap.size });

		if (relatedEntitiesMap.size == 0) {
			return;
		}

		if (activated) {
			unhideRelatedEntities().then(() => {
				if (controllerConfig.showConnector) {
					createRelatedConnections();
				}
				if (controllerConfig.showHighlight) {
					highlightRelatedEntities();
				}
			});
		}
	}

	async function getRelatedEntities(sourceEntitiesArray) {
		for (const sourceEntity of sourceEntitiesArray) {
			if (relatedEntitiesMap.has(sourceEntity)) {
				//sourceEntity already analyzed
				return;
			}

			const unloadedRelatedEntities = getRelatedEntitiesOfSourceEntity(sourceEntity.unloadedRelationships, sourceEntity.type);
			if (unloadedRelatedEntities.length) {
				await neo4jModelLoadController.loadTreesContainingAnyOf(unloadedRelatedEntities);
			}
			const allRelatedEntitiesOfSourceEntity = getRelatedEntitiesOfSourceEntity(sourceEntity, sourceEntity.type);
			const relatedEntitiesOfSourceEntity = [];

			allRelatedEntitiesOfSourceEntity.forEach(function (relatedEntity) {
				if (relatedEntitiesOfSourceEntity.includes(relatedEntity)) {
					events.log.info.publish({ text: "connector - onRelationsChanged - multiple relation" });
					return;
				}

				if (!controllerConfig.showInnerRelations) {
					if (isTargetChildOfSourceParent(relatedEntity, sourceEntity)) {
						events.log.info.publish({ text: "connector - onRelationsChanged - inner relation" });
						return;
					}
				}

				const relation = model.createEntity(
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
			});

			relatedEntitiesMap.set(sourceEntity, relatedEntitiesOfSourceEntity);
		}
	}

	function getRelatedEntitiesOfSourceEntity(sourceEntity, entityType) {
		let relatedEntitiesOfSourceEntity = [];

		switch (entityType) {
			case "Class":
			case "Interface":
				//relatedEntitiesOfSourceEntity = relatedEntitiesOfSourceEntity.concat(sourceEntity.superTypes);
				//relatedEntitiesOfSourceEntity = relatedEntitiesOfSourceEntity.concat(sourceEntity.subTypes);
				break;
			case "ParameterizableClass":
				relatedEntitiesOfSourceEntity = relatedEntitiesOfSourceEntity.concat(sourceEntity.superTypes || []);
				//relatedEntitiesOfSourceEntity = relatedEntitiesOfSourceEntity.concat(sourceEntity.subTypes);
				break;
			case "Attribute":
				relatedEntitiesOfSourceEntity = sourceEntity.accessedBy || [];
				break;
			case "Method":
			case "Function":
				relatedEntitiesOfSourceEntity = sourceEntity.accesses || [];
			case "FunctionModule":
			case "Report":
			case "FormRoutine":
				relatedEntitiesOfSourceEntity = relatedEntitiesOfSourceEntity.concat(sourceEntity.calls || []);
				//relatedEntitiesOfSourceEntity = relatedEntitiesOfSourceEntity.concat(sourceEntity.calledBy);
				break;
			case "Reference":
				relatedEntitiesOfSourceEntity = relatedEntitiesOfSourceEntity.concat(sourceEntity.rcData || []);
				break;
		}

		return relatedEntitiesOfSourceEntity;
	}

	async function getRecursiveRelations(oldSourceEntities) {
		for (const oldSourceEntity of oldSourceEntities) {
			var relatedEntities = relatedEntitiesMap.get(oldSourceEntity);

			if (relatedEntities.length == 0) {
				return;
			}

			newSourceEntities = relatedEntities.filter(relatedEntity => (!relatedEntitiesMap.has(relatedEntity)));

			if (newSourceEntities.length == 0) {
				return;
			}

			await getRelatedEntities(newSourceEntities);
			await getRecursiveRelations(newSourceEntities);
		}
	}


	/*************************
			Connection
	*************************/

	function createRelatedConnections() {

		relations.forEach(function (relation) {
			var sourceEntity = relation.source;
			var relatedEntity = relation.target;

			//create scene element
			let connectorElements = createConnector(sourceEntity, relatedEntity, relation.id);

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

	function createConnector(entity, relatedEntity, relationId) {

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

		const connectorSize = 0.05;

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
		const connector = document.createElement("a-cylinder");
		const connectorColors = [controllerConfig.connectorColor.r, controllerConfig.connectorColor.g, controllerConfig.connectorColor.b];
		connector.setAttribute("color", canvasManipulator.numbersToHexColor(connectorColors.map(v => v*255)));

		connector.addEventListener("loaded", function () {
			const threeMesh = this.object3DMap.mesh;

			threeMesh.scale.set(connectorSize, distance, connectorSize);
			threeMesh.position.set(sourcePosition.x + deltaX / 2,
				sourcePosition.y + deltaY / 2,
				sourcePosition.z + deltaZ / 2);

			connector.setAttribute("radius", 5);

			const quaternion = threeMesh.quaternion;
			quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), direction);

		});
		connector.setAttribute("flat-shading", true);
		connector.setAttribute("shader", "flat");
		connector.setAttribute("id", relationId);

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

		const visibleEntities = Array.from(relatedEntitiesSet.values()).filter(entity => !entity.filtered);
		canvasManipulator.highlightEntities(visibleEntities, controllerConfig.highlightColor, { name: "relationController" });
	}

	function unhighlightRelatedEntities() {
		if (relatedEntitiesSet.size == 0) {
			return;
		}

		const visibleEntities = Array.from(relatedEntitiesSet.values()).filter(entity => !entity.filtered);
		canvasManipulator.unhighlightEntities(visibleEntities, { name: "relationController" });
	}


	function isTargetChildOfSourceParent(target, source) {

		let targetParent = target.belongsTo;
		const sourceParent = source.belongsTo;

		while (targetParent !== undefined) {

			if (targetParent == sourceParent) {
				return true;
			}

			targetParent = targetParent.belongsTo;
		}

		return false;
	}

	async function unhideRelatedEntities() {
		const elementsToUnhide = new Set();
		for (const relatedEntity of relatedEntitiesSet) {
			if (relatedEntity.filtered) {
				elementsToUnhide.add(relatedEntity);
				const hiddenParents = relatedEntity.allParents.filter(entity => entity.filtered);
				for (const parent of hiddenParents) {
					elementsToUnhide.add(parent);
				}
			}
		}
		if (elementsToUnhide.size) {
			const elementsAsArray = [...elementsToUnhide];
			events.filtered.off.publish({
				sender: relationController,
				entities: elementsAsArray
			});

			// re-inserting the elements is synchronous, but they will only be rendered on the next A-Frame tick
			// which entity we wait for here doesn't really matter, it comes down to awaiting the next possible render step
			await canvasManipulator.waitForRenderOfElement(elementsAsArray[0]);
		}
	}


	return {
		initialize: initialize,
		reset: reset,
		activate: activate,
		deactivate: deactivate
	};

}();
