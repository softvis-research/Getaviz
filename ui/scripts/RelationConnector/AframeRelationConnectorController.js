var relationConnectorController = function(){
	
	var sourceEntity = null;
	var relatedEntities = new Array();
	
	var connectors = new Array();
	var relations = new Array();

	var activated = false;
	
	
	//config parameters	
	var controllerConfig = {
        fixPositionY : false,
		fixPositionZ : false,
		showInnerRelations : false,
		sourceStartAtParentBorder : false,
		targetEndAtParentBorder : false,
		sourceStartAtBorder: false,
		targetEndAtBorder: false,
		createEndpoints : false,
		connectorColor : {r: 1, g: 0, b: 0},
		endpointColor : {r: 0, g: 0, b: 0}
	}
	
	
	function initialize(setupConfig){	

		application.transferConfigParams(setupConfig, controllerConfig);
				
		events.selected.on.subscribe(onRelationsChanged);
	}
	
	function activate(){	
		activated = true;
		if(relatedEntities.length != 0){
			createRelatedConnections();
		}
	}

	function deactivate(){
		reset();
		activated = false;
	}
	
	function reset(){
		removeAllConnectors();
	}
	
	function removeAllConnectors(){	
		
		events.log.info.publish({ text: "connector - removeAllConnectors"});

		if( connectors.length == 0){
			return;
		}
		
		//remove scene elements
		connectors.forEach(function(connector){
			canvasManipulator.removeElement(connector);
		});
		
		connectors = new Array();
		
		//remove relation entities
		relations.forEach(function(relation){
			 model.removeEntity(relation);
		});
		
		
		
		//publish removed entities
		var applicationEvent = {			
			sender: relationConnectorController,
			entities: relations
		};
		events.added.off.publish(applicationEvent);
	}
	
	
	
	function onRelationsChanged(applicationEvent) {
		
		events.log.info.publish({ text: "connector - onRelationsChanged"});

		removeAllConnectors();
		
		//get related entities
		sourceEntity = applicationEvent.entities[0];
		
		events.log.info.publish({ text: "connector - onRelationsChanged - selected Entity - " + sourceEntity.name});

		relatedEntities = new Array();
			
		switch(sourceEntity.type) {
			case "Class":
				relatedEntities = relatedEntities.concat(sourceEntity.superTypes);
				relatedEntities = relatedEntities.concat(sourceEntity.subTypes);
				break;
			case  "ParameterizableClass":
				relatedEntities = relatedEntities.concat(sourceEntity.superTypes);
				relatedEntities = relatedEntities.concat(sourceEntity.subTypes);
				break;			
			case "Attribute":
				relatedEntities = sourceEntity.accessedBy;
				break;
			case "Method":
				relatedEntities = sourceEntity.accesses;
				relatedEntities = relatedEntities.concat( sourceEntity.calls );
				relatedEntities = relatedEntities.concat( sourceEntity.calledBy );			
				break;
			
			default: 				
				return;
		}

		events.log.info.publish({ text: "connector - onRelationsChanged - related Entities - " + relatedEntities.length});
		
		if(relatedEntities.length == 0) {
			return;
		}

		if(activated){
			createRelatedConnections();
		}
		
	}


	function createRelatedConnections(){
		var relatedEntitiesMap = new Map();

		relatedEntities.forEach(function(relatedEntity){
			if(relatedEntitiesMap.has(relatedEntity)){
				events.log.info.publish({ text: "connector - onRelationsChanged - multiple relation"});
				return;
			}
			
			if(controllerConfig.showInnerRelations === false){
				if(isTargetChildOfSourceParent(relatedEntity, sourceEntity)){
					events.log.info.publish({ text: "connector - onRelationsChanged - inner relation"});
					return;
				}
			}
								
			//create scene element
			let connectorElements = createConnector(sourceEntity, relatedEntity);
			
			//target or source not rendered -> no connector -> remove relatation
			if( connectorElements === undefined){
				return;
			}

			events.log.info.publish({ text: "connector - onRelationsChanged - create connector"});

			connectorElements.forEach(function(element) {
                connectors.push(element);
			});
			
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
			
			relatedEntitiesMap.set(relatedEntity, relatedEntity);
		});
		
		
		if(relatedEntitiesMap.size != 0){
		
			var applicationEvent = {			
				sender: relationConnectorController,
				entities: relations
			};
			events.added.on.publish(applicationEvent);			
		}

	}

	
	function createConnector(entity, relatedEntity){
		//calculate attributes						
		var sourcePosition = canvasManipulator.getCenterOfEntity(entity);
		if( sourcePosition === null ){
			return;
		}
		
		var targetPosition = canvasManipulator.getCenterOfEntity(relatedEntity);
		
		if( targetPosition === null ){
			return;
		}

        if(controllerConfig.sourceStartAtParentBorder){
        	let sourceParent = entity.belongsTo;
        	let targetParent = relatedEntity.belongsTo;
            if(sourceParent != targetParent){
            	if(controllerConfig.targetEndAtParentBorder) {
            		targetPosition = canvasManipulator.getCenterOfEntity(targetParent);
                }
                let intersection = calculateBorderPosition(targetPosition, canvasManipulator.getCenterOfEntity(sourceParent), sourceParent);
            	if(intersection != undefined) {
            		sourcePosition = intersection;
				} else console.debug("raycasting found no intersection with parent objects surface");
            }
        }

        if(controllerConfig.targetEndAtParentBorder){
        	let targetParent = relatedEntity.belongsTo;
        	if(targetParent != entity.belongsTo) {
        		let intersection = calculateBorderPosition(sourcePosition, canvasManipulator.getCenterOfEntity(targetParent), targetParent);
        		if(intersection != undefined) {
        			targetPostion = intersection;
				} else console.debug("raycasting found no intersection with parent objects surface");
			}
        }

		if( controllerConfig.sourceStartAtBorder ) {
			if(controllerConfig.targetEndAtBorder) {
				targetPosition = canvasManipulator.getCenterOfEntity(relatedEntity);
			}
			// getCenterOfEntity again in-case it got overwritten for sourceStartAtParentBorder
			sourcePosition = calculateBorderPosition(targetPosition, canvasManipulator.getCenterOfEntity(entity), entity);
		}
		if( controllerConfig.targetEndAtBorder ) {
            // getCenterOfEntity again in-case it got overwritten for targetEndAtParentBorder
			targetPosition = calculateBorderPosition(sourcePosition, canvasManipulator.getCenterOfEntity(relatedEntity), relatedEntity);
		}

		/* Workaround for Vive (the coordinates are smaller than in aframe.html of an unknown reason) */
		if (visType === "vive") {
			sourcePosition.x = sourcePosition.x * 1000
			sourcePosition.y = sourcePosition.y * 1000
			sourcePosition.z = sourcePosition.z * 1000
			targetPosition.x = targetPosition.x * 1000
			targetPosition.y = targetPosition.y * 1000
			targetPosition.z = targetPosition.z * 1000
		}

		var connectorSize = 0.05;

        // This function made no sense and doesn't seem to work on x3dom either
		/*if(controllerConfig.fixPositionZ) {
            sourcePosition.z = controllerConfig.fixPositionZ;
            targetPosition.z = controllerConfig.fixPositionZ;
        }*/
		// suggestion for city model: draw horizontal cylinders on the lower positions level
		if(controllerConfig.fixPositionY) {
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
		connector.addEventListener("loaded", function() {
			let threeMesh = this.object3DMap.mesh;

            threeMesh.scale.set(connectorSize, distance, connectorSize);
            threeMesh.material.color.setRGB(controllerConfig.connectorColor.r, controllerConfig.connectorColor.g, controllerConfig.connectorColor.b);
			threeMesh.position.set(sourcePosition.x+deltaX/2,
                sourcePosition.y+deltaY/2,
                sourcePosition.z+deltaZ/2);


            var quaternion = threeMesh.quaternion;
            quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), direction);
		});
		connector.setAttribute("flat-shading", true);
		connector.setAttribute("shader", "flat");



		let scene = document.querySelector("a-scene");
		scene.appendChild(connector);
		var connectorElements = [];
		connectorElements.push(connector);

		// create Endpoints
        if(controllerConfig.createEndpoints) {
        	var size = connectorSize*1.5;
        	var length = size * 6;
            var sourceEndpoint = document.createElement("a-cylinder");
            sourceEndpoint.addEventListener("loaded", function() {
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
            targetEndpoint.addEventListener("loaded", function() {
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

    function isTargetChildOfSourceParent(target, source){

        var targetParent = target.belongsTo;
        var sourceParent = source.belongsTo;

        while(targetParent !== undefined) {

            if(targetParent == sourceParent){
                return true;
            }

            targetParent = targetParent.belongsTo;
        }

        return false;
    }

    function calculateBorderPosition(sourceOfRay, targetOfRay, entity){
		let object = document.getElementById(entity.id);
		let raycaster = new THREE.Raycaster();
		raycaster.set(sourceOfRay, targetOfRay.subVectors(targetOfRay, sourceOfRay).normalize());
		let intersection = raycaster.intersectObject(object.object3DMap.mesh);
		return intersection[0].point;
	}

	return {
        initialize		: initialize,
		reset			: reset,
		activate		: activate,
		deactivate		: deactivate
    };    

}();