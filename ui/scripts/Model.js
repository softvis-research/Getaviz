var model = (function() {

	//states
	const states = {
		selected 		: { name: "selected" },
		marked 			: { name: "marked" },
		hovered 		: { name: "hovered" },
		filtered 		: { name: "filtered" },
        tmpFiltered     : { name: "tmpFiltered"},
		added			: { name: "added" },
		componentSelected : { name: "componentSelected" },
		antipattern     : { name: "antipattern" },
		versionSelected : { name: "versionSelected" }
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

	function initialize(famixModel) {            
		//create initial entites from famix elements 
		famixModel.forEach(function(element) {
			
			if(element.type === undefined){
				console.log("element.type undefined");
			}

			let entity = createEntity(
				element.type.substring(element.type.indexOf(".") + 1), 
				element.id, 
				element.name, 
				element.qualifiedName, 
				element.belongsTo,
                element.antipattern,
                element.roles,
				element.isTransparent,
				element.version
			);
			
			entity.isTransparent = false;
						
			switch(entity.type) {
				case "text":
                    entity.versions = element.versions.split(",");
                    for(let i = 0; i < entity.versions.length; ++i) {
                        entity.versions[i] = entity.versions[i].trim();
                    }
                    entity.versions.forEach(function(version){
                        if(version !== undefined) {
                            if(entitiesByVersion.has(version)) {
                                let map = entitiesByVersion.get(version);
                                map.push(entity);
                                entitiesByVersion.set(version, map);
                            } else {
                                addVersion(version);
                                let map = [];
                                map.push(entity);
                                entitiesByVersion.set(version, map);
                            }
                        }
                    });
                    labels.push(entity);
                    break;
				case "issue":
					entity.open = (element.open === "true");
                    entity.security = (element.security === "true");
					entity.qualifiedName = entity.id;
					issues.push(entity);
                    issuesById.set(entity.id, entity);
					break;

				case "path":
					entity.start = element.start;
					entity.end = element.end;
					entity.role = element.role;
					paths.push(entity);
					break;
                case "stk":
                    entity.versions = element.versions.split(",");
                    for(let i = 0; i < entity.versions.length; ++i) {
                    	entity.versions[i] = entity.versions[i].trim();
                    }
                    return;
				case "component": 
					entity.components = element.components.split(",");
					entity.versions = element.versions.split(",");
					return;
				case "Project" :
				case "Namespace":
					entity.version = element.version;
					if(entity.version !== undefined) {
						if(entitiesByVersion.has(entity.version)) {
							let map = entitiesByVersion.get(entity.version);
							map.push(entity);
							entitiesByVersion.set(entity.version, map);
						} else {
							addVersion(entity.version);
							let map = [];
							map.push(entity);
							entitiesByVersion.set(entity.version, map);
						}
					}
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
					entity.antipattern = element.antipattern.split(",");
					entity.roles = element.roles.split(",");
					entity.component = element.component;
                    entity.version = element.version;
					entity.betweennessCentrality = element.betweennessCentrality;
					entity.changeFrequency = element.changeFrequency;
					if(entity.version !== undefined) {
						if(entitiesByVersion.has(entity.version)) {
							let map = entitiesByVersion.get(entity.version);
							map.push(entity);
							entitiesByVersion.set(entity.version, map);
						} else {
							addVersion(entity.version);
							let map = [];
							map.push(entity);
							entitiesByVersion.set(entity.version, map);
						}
					}
					if(element.issues !== undefined) {
                        entity.issues = element.issues.split(",");
                    } else {
						entity.issues = [];
					}
                  	for(let i = 0; i < entity.issues.length; ++i) {
                        entity.issues[i] = entity.issues[i].trim();
                    }
                    entity.issues.forEach(function(issue) {
                        if(entitiesByIssue.has(issue)) {
                            let map = entitiesByIssue.get(issue);
                        	map.push(entity);
                            entitiesByIssue.set(issue, map);
                        } else {
                            addIssue(issue);
                            let map = [];
                            map.push(entity);
                            entitiesByIssue.set(issue, map);
                        }
                    });
					entity.numberOfOpenIssues = element.numberOfOpenIssues;
					entity.numberOfClosedIssues = element.numberOfClosedIssues;
					entity.numberOfClosedSecurityIssues = element.numberOfClosedSecurityIssues;
					entity.numberOfOpenSecurityIssues = element.numberOfOpenSecurityIssues;

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
				case "Function":
					entity.signature = element.signature;
					entity.qualifiedName = element.qualifiedName;
					
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
				case "Variable":
					if(element.accessedBy){
						entity.accessedBy = element.accessedBy.split(",");
					} else {
						entity.accessedBy = [];
					}
					
					if(element.declaredType){
						//if variable is of type array, [] is put after the variable name
						if(element.declaredType.includes("[")){
							let parts = element.declaredType.split("[");
							entity.displayText = parts[0] + entity.name + "[" + parts[1];
						} else {
							entity.displayText = element.declaredType + " " + element.name;
						}
					} else {
						entity.displayText = element.name;
					}
					break;
				case "TranslationUnit":
					break;
				default: 
					return;
			}
						
			entitiesById.set(element.id, entity);
		});

		//set object references
		entitiesById.forEach(function(entity) {
			
			if(entity.belongsTo === undefined || entity.belongsTo === "root" ){
				delete entity.belongsTo;
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
                            
                case "component":
                    let components = [];
                    entity.components.forEach(function(componentId) {
                       const relatedEntity = entitiesById.get(componentId.trim());
                       if(relatedEntity !== undefined) {
                           components.push(relatedEntity);
                       }
                    });
                    entity.components = components;
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
                                        
                    let reaches = [];
					entity.reaches.forEach(function(reachesId){
						const relatedEntity = entitiesById.get(reachesId.trim());
						if(relatedEntity !== undefined){
							reaches.push(relatedEntity);
							relatedEntity.reachedBy.push(entity);
						}
					});
					entity.reaches = reaches;
					let antipatterns = [];
					entity.antipattern.forEach(function(antipatternID
					) {
						let antipattern = entitiesById.get(antipatternID.trim());
						if(antipattern !== undefined) {
							antipatterns.push(antipattern);
						}
					});
					entity.antipattern = antipatterns;
					
					let roles = [];
					entity.roles.forEach(function(roleID
					) {
						//var role = entitiesById.get(roleID.trim());
						const role = roleID.trim();
						if(role !== undefined) {
							roles.push(role);
						}
					});
					entity.roles = roles;
					
					break;
				
				case  "ParameterizableClass":
					superTypes = [];
					entity.superTypes.forEach(function(superTypeId){
						let relatedEntity = entitiesById.get(superTypeId.trim());
						if(relatedEntity !== undefined){
							superTypes.push(relatedEntity);
						}
					});
					entity.superTypes = superTypes;
					
					subTypes = [];
					entity.subTypes.forEach(function(subTypesId){
						let relatedEntity = entitiesById.get(subTypesId.trim());
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
				
				default: 				
					return;
			}
		});

        //set all parents attribute
		entitiesById.forEach(function(entity) {
			entity.allParents = getAllParentsOfEntity(entity);
		});
		
						
						
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
	
	function reset(){
		eventEntityMap.forEach(function(entityMap, eventKey, map){
			entityMap.forEach(function(entity, entityId){
				entity[eventKey.name] = false;	
			});
			entityMap.clear();			
		});
	}
	
	
	
	function createEntity(type, id, name, qualifiedName, belongsTo){
		let entity = {
			type: type,
			id: id,
			name: name,
			qualifiedName: qualifiedName,
			belongsTo: belongsTo,
			children: []						
		};
		
		const statesArray = Object.keys(states);
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
		return entitiesById.get(id);
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
