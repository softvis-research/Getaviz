var relationConnectorController = function(){
	
	var sourceEntity = null;
	var relatedEntities = new Array();
	
	var connectors = new Array();
	var relations = new Array();
	
	var loadedMin		= new Map();
	var loadedMax		= new Map();
	var loadedPositions = new Map();
	var loadedDistances = new Map();

	var activated = false;
	
	
	//config parameters	
	var controllerConfig = {
		fixPositionZ : false,
		showInnerRelations : false,
		elementShape : "",					//circle, square
		sourceStartAtParentBorder : false,
		targetEndAtParentBorder : false,
		sourceStartAtBorder: false,
		targetEndAtBorder: false,
		createEndpoints : true,
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
		if( controllerConfig.sourceStartAtBorder ) {
			sourcePosition = calculateBorderPosition(targetPosition, sourcePosition, entity);
		}
		if( controllerConfig.targetEndAtBorder ) {
			targetPosition = calculateBorderPosition(sourcePosition, targetPosition, relatedEntity);
		}
		
		var connectorColor = {r:1, g:1, b:0};
		var connectorSize = 0.05;

		//config
		if(controllerConfig.fixPositionZ) {
            sourcePosition[z] = controllerConfig.fixPositionZ;
            targetPosition[z] = controllerConfig.fixPositionZ;
        }


        let deltaX = targetPosition.x - sourcePosition.x;
        let deltaY = targetPosition.y - sourcePosition.y;
        let deltaZ = targetPosition.z - sourcePosition.z;

        let distance = sourcePosition.distanceTo(targetPosition);
        let direction = new THREE.Vector3(deltaX, deltaY, deltaZ).normalize();

		//create element
        var connector = document.createElement("a-cylinder");
		connector.addEventListener("loaded", function() {
			let threeMesh = this.object3DMap.mesh;

            threeMesh.scale.set(connectorSize, distance, connectorSize);
            threeMesh.material.color.setRGB(connectorColor.r, connectorColor.g, connectorColor.b);
			threeMesh.position.set(sourcePosition.x+deltaX/2,
                sourcePosition.y+deltaY/2,
                sourcePosition.z+deltaZ/2);


            var quaternion = threeMesh.quaternion;
            quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), direction);
		});



		let scene = document.querySelector("a-scene");
		scene.appendChild(connector);
		var connectorElements = [];
		connectorElements.push(connector);
        if(controllerConfig.createEndpoints) {
        	var size = connectorSize*1.5;
        	var length = size * 6;
            var sourceEndPoint = document.createElement("a-cylinder");
            sourceEndPoint.addEventListener("loaded", function() {
                let threeMesh = this.object3DMap.mesh;
                threeMesh.material.color.setRGB(connectorColor.r, connectorColor.g, connectorColor.b);
                threeMesh.scale.set(size, length, size);
                threeMesh.position.set(sourcePosition.x, sourcePosition.y, sourcePosition.z);
                var quaternion = threeMesh.quaternion;
                quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), direction);
            });
            var targetEndPoint = document.createElement("a-cylinder");
            targetEndPoint.addEventListener("loaded", function() {
                let threeMesh = this.object3DMap.mesh;
                threeMesh.material.color.setRGB(connectorColor.r, connectorColor.g, connectorColor.b);
                threeMesh.material.needsUpdate = true;
                console.debug(threeMesh.material);
                threeMesh.scale.set(size, length, size);
                threeMesh.position.set(targetPosition.x, targetPosition.y, targetPosition.z);
                var quaternion = threeMesh.quaternion;
                quaternion.setFromUnitVectors(new THREE.Vector3(0, 1, 0), direction.normalize());
            });
            scene.appendChild(sourceEndPoint);
            scene.appendChild(targetEndPoint);
            connectorElements.push(sourceEndPoint);
            connectorElements.push(targetEndPoint);

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

	function calculateBorderPosition(sourcePosition, targetPosition, entity){
		let object = document.getElementById(entity.id);
		let raycaster = new THREE.Raycaster();
		raycaster.set(sourcePosition, targetPosition.subVectors(targetPosition, sourcePosition).normalize());
		let intersection = raycaster.intersectObject(object.object3DMap.mesh);
		return intersection[0].point;
	}
	
	/*function calculatePositionFromParent(sourcePosition, targetPosition, sourceParent){
		if(controllerConfig.elementShape == "circle"){
			return calculateCirclePositionFromParent(sourcePosition, targetPosition, sourceParent);
		}
		if(controllerConfig.elementShape == "square"){
			return calculateSquarePositionFromParent(sourcePosition, targetPosition, sourceParent);
		}
		return sourcePosition;
	}
	
	function calculateSquarePositionFromParent(sourcePosition, targetPosition, sourceParent){
		// To implement...
	}
	
	function calculateCirclePositionFromParent(sourcePosition, targetPosition, sourceParent){
		//calculation derived from http://www.3d-meier.de/tut6/XPresso53.html
		
		var parentPosition = getObjectPosition(sourceParent.id);
		
		var parentRadius = loadedDistances.get(sourceParent.id);
		var parentX = parentPosition[0];
		var parentY = parentPosition[1];
			
		
		var targetX = targetPosition[0];
		var targetY = targetPosition[1];
		
		var sourceX = sourcePosition[0];
		var sourceY = sourcePosition[1];
				
		var deltaX = targetX - sourceX;	
		var deltaY = targetY - sourceY;	
		
		var a = deltaY / deltaX;
		var b = (targetY - parentY) - ( a * (targetX - parentX) );
						
		var r = parentRadius[0];
		
		
		var AA = 1 + Math.pow(a, 2);
		var BB = (2 * a * b)
		var CC = Math.pow(b, 2) - Math.pow(r, 2);
				
		var XX = Math.pow(BB, 2) - 4 * AA * CC;
		 
		
		var x1 = (-BB + Math.sqrt( XX, 2 )) / ( 2 * AA );
		var x2 = (-BB - Math.sqrt( XX, 2 )) / ( 2 * AA );
		
		var y1 = a * x1 + b;
		var y2 = a * x2 + b;		
		
		
		var newSourcePosition;
		if(  	(targetY > sourceY && targetX < sourceX) ||
				(targetY < sourceY && targetX < sourceX) ){
			newSourcePosition	= [x2+parentX, y2+parentY, sourcePosition[2]];			
		} else {
			newSourcePosition	= [x1+parentX, y1+parentY, sourcePosition[2]];
		}
				
		return newSourcePosition;
	}*/
	

	return {
        initialize		: initialize,
		reset			: reset,
		activate		: activate,
		deactivate		: deactivate
    };    

}();