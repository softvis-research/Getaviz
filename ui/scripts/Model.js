var model = (function() {

	//states
	var states = {
		selected 		: { name: "selected" },
		marked 			: { name: "marked" },
		hovered 		: { name: "hovered" },
		filtered 		: { name: "filtered" },
		added			: { name: "added" }
	};


	

	var entitiesById = new Map();
	var eventEntityMap = new Map();
	
	function initialize(famixModel) {
         
		//create initial entites from famix elements 
		famixModel.forEach(function(element) {
			
			if(element.type == undefined){
				console.log("element.type undefined");
			}
			
			var entity = createEntity(
				element.type.substring(element.type.indexOf(".") + 1), 
				element.id, 
				element.name, 
				element.qualifiedName, 
				element.belongsTo
			);
						
			switch(entity.type) {
				case "Class":
					entity.superTypes = element.subClassOf.split(",");
					entity.subTypes = element.superClassOf.split(",");
					break;
				case  "ParameterizableClass":
					entity.superTypes = element.subClassOf.split(",");
					entity.subTypes = element.superClassOf.split(",");
					break;			
				case "Attribute":
					if(element.accessedBy){
						entity.accessedBy = element.accessedBy.split(",");
					} else {
						entity.accessedBy = [];
					}
					break;
				case "Method":
					entity.signature = element.signature;
					
					var pathParts = entity.qualifiedName.split("_");
					var pathString = pathParts[0];					
					var path = pathString.split(".");
					path = path.splice(0, path.length - 1);
					var methodSignature = entity.signature.split(" ");
					methodSignature = methodSignature.splice(1, methodSignature.length);
					
					entity.qualifiedName = "";
					path.forEach(function(pathPart){
						entity.qualifiedName = entity.qualifiedName + pathPart + ".";
					});
					methodSignature.forEach(function(methodSignaturePart){
						entity.qualifiedName = entity.qualifiedName + methodSignaturePart + " ";
					});
					
					entity.qualifiedName = entity.qualifiedName.trim();
					
					if(element.calls){
						entity.calls = element.calls.split(",");
					} else {
						entity.calls = [];
					}
					if(element.calledBy){
						entity.calledBy = element.calledBy.split(",");
					} else {
						entity.calledBy = [];
					}
					if(element.accesses){						
						entity.accesses = element.accesses.split(",");
					} else {
						entity.accesses = [];
					}
					break;				
				default: 				
					return;
			}
						
			entitiesById.set(element.id, entity);			
		});

			
		//set object references
		entitiesById.forEach(function(entity) {
			
			if(entity.belongsTo == undefined || entity.belongsTo == "root" ){
				delete entity.belongsTo;
			} else {
				var parent = entitiesById.get(entity.belongsTo);		
				if(parent === undefined)		{
					events.log.error.publish({ text: "Parent of " + entity.name + " not defined" });
				} else {
					entity.belongsTo = parent;
					parent.children.push(entity);
				}
			}	
			
			switch(entity.type) {
				case "Class":
					var superTypes = new Array();
					entity.superTypes.forEach(function(superTypeId){
						var relatedEntity = entitiesById.get(superTypeId.trim());
						if(relatedEntity !== undefined){
							superTypes.push(relatedEntity);
						}
					});
					entity.superTypes = superTypes;
					
					var subTypes = new Array();
					entity.subTypes.forEach(function(subTypesId){
						var relatedEntity = entitiesById.get(subTypesId.trim());
						if(relatedEntity !== undefined){
							subTypes.push(relatedEntity);
						}
					});
					entity.subTypes = subTypes;		
					
					break;
				
				case  "ParameterizableClass":
					var superTypes = new Array();
					entity.superTypes.forEach(function(superTypeId){
						var relatedEntity = entitiesById.get(superTypeId.trim());
						if(relatedEntity !== undefined){
							superTypes.push(relatedEntity);
						}
					});
					entity.superTypes = superTypes;
					
					var subTypes = new Array();
					entity.subTypes.forEach(function(subTypesId){
						var relatedEntity = entitiesById.get(subTypesId.trim());
						if(relatedEntity !== undefined){
							subTypes.push(relatedEntity);
						}
					});
					entity.subTypes = subTypes;		
					
					break;			
				
				case "Attribute":	
					var accessedBy = new Array();
					entity.accessedBy.forEach(function(accessedById){
						var relatedEntity = entitiesById.get(accessedById.trim());
						if(relatedEntity !== undefined){
							accessedBy.push(relatedEntity);
						}
					});
					entity.accessedBy = accessedBy;					
					
					break;
				
				case "Method":
					var calls = new Array();
					entity.calls.forEach(function(callsId){
						var relatedEntity = entitiesById.get(callsId.trim());
						if(relatedEntity !== undefined){
							calls.push(relatedEntity);
						}
					});
					entity.calls = calls;
					
					var calledBy = new Array();
					entity.calledBy.forEach(function(calledById){
						var relatedEntity = entitiesById.get(calledById.trim());
						if(relatedEntity !== undefined){
							calledBy.push(relatedEntity);
						}
					});
					entity.calledBy = calledBy;
					
					var accesses = new Array();
					entity.accesses.forEach(function(accessesId){
						var relatedEntity = entitiesById.get(accessesId.trim());
						if(relatedEntity !== undefined){
							accesses.push(relatedEntity);
						}
					});
					entity.accesses = accesses;
					
					break;				
				
				default: 				
					return;
			}
			
			
		});
		
		//set all parents attribute
		entitiesById.forEach(function(entity) {
			entity.allParents = getAllParentsOfEntity(entity);
		});
		
						
						
		//subscribe for changing status of entities on events
		var eventArray = Object.keys(states);			
		eventArray.forEach(function(eventName){
			
			var event = events[eventName];
			
			var eventMap = new Map();
			eventEntityMap.set(event, eventMap);
			
			event.on.subscribe(function(applicationEvent){
				applicationEvent.entities.forEach(function(entity){
					entity[event.name] = true;				
					eventMap.set(entity.id, entity);
				});				
			});		
			
			event.off.subscribe(function(applicationEvent){
				applicationEvent.entities.forEach(function(entity){
					entity[event.name] = false;
					eventMap.delete(entity.id);
				});
			});		
		});
    }
	
	
	
	
	function reset(){
		eventEntityMap.forEach(function(entityMap, eventKey, map){
			entityMap.forEach(function(entity, entityId){
				entity[eventKey.name] = false;	
			});
			entityMap.clear();			
		});
	}
	
	
	
	function createEntity(type, id, name, qualifiedName, belongsTo){
		var entity = {
			type: type,
			id: id,
			name: name,
			qualifiedName: qualifiedName,
			belongsTo: belongsTo,
			children: []						
		};		
		
		var statesArray = Object.keys(states);		
		statesArray.forEach(function(stateName){
			entity[stateName] = false;
		});	
		
		entitiesById.set(id, entity);
		
		return entity;
	}
	
	function removeEntity(id){
		entitiesById.delete(id);
	}
	
	
	
	function getAllParentsOfEntity(entity){
		var parents = new Array();
		
		if(entity.belongsTo !== undefined){
			var parent = entity.belongsTo;
			parents.push(parent);
			
			var parentParents = getAllParentsOfEntity(parent);			
			parents = parents.concat(parentParents);			
		}				
	
		return parents;
	}

	
	function getAllEntities(){
		return entitiesById;
	}
	
	function getEntityById(id){
		return entitiesById.get(id);
	}	
	
	function getEntitiesByState(stateEventObject){
		return eventEntityMap.get(stateEventObject);
	}
	
	
	
	return {
        initialize					: initialize,
		reset						: reset,
		states						: states,
		
		getAllEntities				: getAllEntities,
		getEntityById				: getEntityById,
		getEntitiesByState			: getEntitiesByState,


		createEntity				: createEntity,
		removeEntity				: removeEntity
    };
	
})();