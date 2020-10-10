var model = (function() {

	//states
	const states = {
		// TODO: wasSelected, wasExpanded
		selected 		: { name: "selected" },
		marked 			: { name: "marked" },
		hovered 		: { name: "hovered" },
		filtered 		: { name: "filtered" },
        tmpFiltered     : { name: "tmpFiltered"},
		added			: { name: "added" },
		componentSelected : { name: "componentSelected" },
		antipattern     : { name: "antipattern" },
		versionSelected : { name: "versionSelected" },
		loaded			: { name: "loaded" }, // Element is added to DOM
		childsLoaded	: { name: "childsLoaded" } // Child elements are also loaded
    };

	let entitiesById = new Map();
	let eventEntityMap = new Map();
    let entitiesByVersion = new Map();
    let entitiesByIssue = new Map();
	let selectedVersions = [];
	let selectedIssues = [];
	let issues = [];
	let issuesById = new Map();
	let paths = [];
	let labels = [];

	// Called from Application.js, because Event.js is loaded after Model.js
	function initialize() {
		//subscribe for changing status of entities on events
		let eventArray = Object.keys(states);
		eventArray.forEach(function(eventName){
			
			let event = events[eventName];
			
			let eventMap = new Map();
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

	function createEntities(elements = []) {
		let newEntities = [];            
		//create initial entites from famix elements 
		elements.forEach(function(element) {
			// May happen, if it's dummy element (sub-distrikt)
			if (element == null) {
				return;
			}
			let entityAlreadyInModel = getEntityById(element.id);
			if (entityAlreadyInModel) {
				return;
			}

			if(element.type === undefined){
				console.log("element.type undefined");
			}

			let entity = createEntity(element);
			entitiesById.set(entity.id, entity);
			newEntities.push(entity);
		});

		//set object references - if set wrong, package Explorer will show wrong structure
		newEntities.forEach(function(entity) {
			if(entity.belongsTo === undefined || entity.belongsTo === "root" || entity.belongsTo === "" ){
				delete entity.belongsTo;
			} else if (typeof (entity.belongsTo) === 'object') {

			} else {
				let parent = entitiesById.get(entity.belongsTo);
				if(parent === undefined)		{
					events.log.error.publish({ text: "Parent of " + entity.name + " not defined" });
				} else {
					entity.belongsTo = parent;
					parent.children.push(entity);
				}
			}

			let superTypes = [];
			let subTypes = [];
			
			switch(entity.type) {
				case "Project":
                case "text":
                    break;
				case "issue":
					break;                              
				case "Class":
					superTypes = [];
					entity.superTypes.forEach(function(superTypeId){
						const relatedEntity = entitiesById.get(superTypeId.trim());
						if(relatedEntity !== undefined){
							superTypes.push(relatedEntity);
						}
					});
					entity.superTypes = superTypes;
					
					subTypes = [];
					entity.subTypes.forEach(function(subTypesId){
						const relatedEntity = entitiesById.get(subTypesId.trim());
						if(relatedEntity !== undefined){
							subTypes.push(relatedEntity);
						}
					});
					entity.subTypes = subTypes;	

					break;		
				
				case "Attribute":	
					let accessedBy = [];
					entity.accessedBy.forEach(function(accessedById){
						let relatedEntity = entitiesById.get(accessedById.trim());
						if(relatedEntity !== undefined){
							accessedBy.push(relatedEntity);
						}
					});
					entity.accessedBy = accessedBy;					
					
					break;
				
				case "Method":
					let calls = [];
					entity.calls.forEach(function(callsId){
						let relatedEntity = entitiesById.get(callsId.trim());
						if(relatedEntity !== undefined){
							calls.push(relatedEntity);
						}
					});
					entity.calls = calls;
					
					let calledBy = [];
					entity.calledBy.forEach(function(calledById){
						let relatedEntity = entitiesById.get(calledById.trim());
						if(relatedEntity !== undefined){
							calledBy.push(relatedEntity);
						}
					});
					entity.calledBy = calledBy;
					
					let accesses = [];
					entity.accesses.forEach(function(accessesId){
						let relatedEntity = entitiesById.get(accessesId.trim());
						if(relatedEntity !== undefined){
							accesses.push(relatedEntity);
						}
					});
					entity.accesses = accesses;
					
					break;				
				
				default:;
			}
		});

        
		newEntities.forEach(function(entity) {
			entity.allParents = getAllParentsOfEntity(entity);
		});				

		// Add Element to DOM and refresh controllers
		let applicationEvent = {			
			sender: 	 model,
			entities:    newEntities,
			adjustments: {checked: false},// to check the element in PackageExplorer
			callback:    ['addTreeNode']
		};
		
		events.loaded.on.publish(applicationEvent);
		return newEntities;
    }	
	
	
	function reset(){
		eventEntityMap.forEach(function(entityMap, eventKey, map){
			entityMap.forEach(function(entity, entityId){
				entity[eventKey.name] = false;	
			});
			entityMap.clear();			
		});
	}

	function createEntity(element){
		let entity = {
			type: element.type.substring(element.type.indexOf(".") + 1),
			id: element.id, 
			name: element.name, 
			qualifiedName: element.qualifiedName, 
			belongsTo: element.belongsTo,
			antipattern: element.antipattern,
			roles: element.roles,
			isTransparent: element.isTransparent,
			version: element.version,
			children: []						
		};
		
		entity.isTransparent = false;
		const statesArray = Object.keys(states);
		statesArray.forEach(function(stateName){
			return entity[stateName] = false;
		});
		
		switch(entity.type) {
			case "Project" :
			case "Namespace":
				break;

			case "Class":
				entity.superTypes = element.subClassOf.split(",");
				entity.subTypes = element.superClassOf.split(",");
				if(element.reaches !== undefined) {
					entity.reaches = element.reaches.split(",");
				} else {
					entity.reaches = [];
				}
				entity.reachedBy = [];
				if(entity.antipattern !== false) {
					entity.antipattern = element.antipattern.split(",");
				} else {
					entity.antipattern = [];
				}
				if(entity.roles !== undefined) {
					entity.roles = element.roles.split(",");
				} else {
					entity.roles = [];
				}
				entity.component = element.component;
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
				
				let pathParts = entity.qualifiedName.split("_");
				let pathString = pathParts[0];
				let path = pathString.split(".");
				path = path.splice(0, path.length - 1);
				let methodSignature = entity.signature.split(" ");
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
			default:;
		}

		return entity;
	}
	
	function removeEntity(id){
		entitiesById.delete(id);
	}
	
	
	function getAllParentsOfEntity(entity){
		let parents = [];
		
		if(entity.belongsTo !== undefined && entity.belongsTo !== ""){
			const parent = entity.belongsTo;
			parents.push(parent);
			
			const parentParents = getAllParentsOfEntity(parent);
			parents = parents.concat(parentParents);			
		}				
	
		return parents;
	}
	
	function getAllEntities(){
		return entitiesById;
	}
	
	function getEntityById(id){
		let entity = entitiesById.get(id);
		return entity;
	}

    function getIssuesById(id){
        return issuesById.get(id);
    }
	
	function getAllVersions() {
		return entitiesByVersion;
	}

    function getAllIssues() {
        return issues;
	}

	function getAllSecureEntities(){
	    let entities = [];
	    entitiesById.forEach(function(entity){
            if(entity.type === "Class" && entity.numberOfOpenSecurityIssues === 0){
                entities.push(entity);
            }
        });
	    return entities;
    }

    function getAllCorrectEntities(){
        let entities = [];
        entitiesById.forEach(function(entity){
            if(entity.type === "Class" && entity.numberOfOpenIssues === 0 && entity.numberOfOpenSecurityIssues === 0){
                entities.push(entity);
            }
        });
        return entities;
    }
	
	function getEntitiesByComponent(component) {
            let entities = [];
            entitiesById.forEach(function(entity) {
                if(entity.component === component) {
                    entities.push(entity);
                }
            });
            return entities;
        }

        function getRole(start, pattern) {
	        let result = "";
            paths.forEach(function(path){
                if(start === path.start && path.belongsTo.id === pattern) {
                   result = path.role;
                }
            });
            return result;
		}

    function getRoleBetween(start, end) {
        for(let i = 0; i < paths.length; ++i) {
			const path = paths[i];
            if(path.start === start && path.end === end) {
                return path.role;
            }
        }
    }

        function getPaths(start, pattern) {
			let targets = [];
			paths.forEach(function(path){
				if(start === path.start && path.belongsTo.id === pattern) {
					targets.push((path.end));
				}
			});
			return targets;
		}

        function getEntitiesByAntipattern(antipatternID) {
            let entities = [];
            entitiesById.forEach(function(entity) {
                let antipattern = [];
                if(entity.type === "Class") {
                    antipattern = entity.antipattern;
                    for(let i = 0; i < antipattern.length; i++) {
                        if(antipattern[i].id === antipatternID) {
                            entities.push(entity);
                        }
                    }
                }
            });
            return entities;
        }
        
    function removeVersion(version) {
        const index = selectedVersions.indexOf(version);
        if (index > -1) {
            selectedVersions.splice(index, 1);
        }
    }

    function removeIssue(issue) {
        const index = selectedIssues.indexOf(issue);
        if (index > -1) {
            selectedIssues.splice(index, 1);
        }
    }
        
    function addVersion(version) {
        selectedVersions.push(version);
    }

    function addIssue(issue) {
		selectedIssues.push(issue);
	}
	
	function getEntitiesByState(stateEventObject){
		return eventEntityMap.get(stateEventObject);
	}
	
	function getEntitiesByVersion(versionId){
        return entitiesByVersion.get(versionId);
    }

    function getEntitiesByIssue(issue){
        return entitiesByIssue.get(issue);
    }

    function getEntitiesByType(type) {
		let entities = [];
		entitiesById.forEach(function(value){
			if(value.type === type){
				entities.push(value)
			}
		});
		return entities;
	}



    function getLabels(){
	    return labels;
    }
	
	function getSelectedVersions() {
		return selectedVersions;
	}
	
	return {
		initialize					: initialize,
        createEntities				: createEntities,
		reset						: reset,
		states						: states,
		
		getAllEntities				: getAllEntities,
        getAllSecureEntities        : getAllSecureEntities,
        getAllCorrectEntities       : getAllCorrectEntities,
        getEntityById				: getEntityById,
		getEntitiesByState			: getEntitiesByState,
		getEntitiesByComponent		: getEntitiesByComponent,
		getEntitiesByAntipattern	: getEntitiesByAntipattern,
		getEntitiesByVersion		: getEntitiesByVersion,
		getEntitiesByIssue			: getEntitiesByIssue,
		getEntitiesByType			: getEntitiesByType,
        getAllParentsOfEntity       : getAllParentsOfEntity,
        getAllVersions              : getAllVersions,
		getAllIssues				: getAllIssues,
        getIssuesById               : getIssuesById,
		createEntity				: createEntity,
		removeEntity				: removeEntity,

		addVersion                  : addVersion,
		removeVersion               : removeVersion,
		addIssue					: addIssue,
		removeIssue					: removeIssue,
		getSelectedVersions			: getSelectedVersions,
		getPaths					: getPaths,
        getRole 					: getRole,
		getRoleBetween				: getRoleBetween,
        getLabels                   : getLabels
    };
	
})();
