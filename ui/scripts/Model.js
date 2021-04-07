var model = (function () {

	//states
	const states = {
		selected: { name: "selected" },
		marked: { name: "marked" },
		hovered: { name: "hovered" },
		filtered: { name: "filtered" },
		tmpFiltered: { name: "tmpFiltered" },
		added: { name: "added" },
		componentSelected: { name: "componentSelected" },
		antipattern: { name: "antipattern" },
		versionSelected: { name: "versionSelected" },
		macroChanged: { name: "macroChanged" },
		loaded: { name: "loaded" },
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
	let macrosById = new Map();
	let modelElementsByMacro = new Map();
	let entitiesByContainedUnloadedProperty = new Map();

	function initialize() {
		//subscribe for changing status of entities on events
		let eventArray = Object.keys(states);
		eventArray.forEach(function (eventName) {

			let event = events[eventName];

			let eventMap = new Map();
			eventEntityMap.set(event, eventMap);

			event.on.subscribe(function (applicationEvent) {
				applicationEvent.entities.forEach(function (entity) {
					entity[event.name] = true;
					eventMap.set(entity.id, entity);
				});
			});

			event.off.subscribe(function (applicationEvent) {
				applicationEvent.entities.forEach(function (entity) {
					entity[event.name] = false;
					eventMap.delete(entity.id);
				});
			});
		});
	}

	function createEntititesFromMetadata(metadataArray, areChildrenLoaded = true) {
		const newElements = [];
		metadataArray.forEach(function (element) {
			if (element.type === undefined) {
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

			entity.hasUnloadedChildren = !areChildrenLoaded;

			if (element.created) {
				//format: YYYY-MM-DD
				var dateString = element.created.toString().slice(0, 4) + "-" + element.created.toString().slice(4, 6) + "-" + element.created.toString().slice(6, 8);

				entity.dateOfCreation = new Date(dateString);
			} else {
				entity.dateOfCreation = new Date(0);
			}

			if (element.changed) {
				//format: YYYY-MM-DD
				var dateString = element.changed.toString().slice(0, 4) + "-" + element.changed.toString().slice(4, 6) + "-" + element.changed.toString().slice(6, 8);

				entity.dateOfLastChange = new Date(dateString);
			} else {
				entity.dateOfLastChange = new Date(0);
			}

			switch (entity.type) {
				case "text":
					entity.versions = element.versions.split(",");
					for (let i = 0; i < entity.versions.length; ++i) {
						entity.versions[i] = entity.versions[i].trim();
					}
					entity.versions.forEach(function (version) {
						if (version !== undefined) {
							if (entitiesByVersion.has(version)) {
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
					for (let i = 0; i < entity.versions.length; ++i) {
						entity.versions[i] = entity.versions[i].trim();
					}
					return;
				case "component":
					entity.components = element.components.split(",");
					entity.versions = element.versions.split(",");
					return;
				case "Project":
				case "Namespace":
					entity.version = element.version;
					if (entity.version !== undefined) {
						if (entitiesByVersion.has(entity.version)) {
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
                case "Reference":
					if (element.rcData) {
						entity.rcData = element.rcData.split(",");
					} else {
						entity.rcData = [];
					}
					break;
				case "Class":
				case "Interface":
					entity.superTypes = element.subClassOf.split(",");
					entity.subTypes = element.superClassOf.split(",");
					if (element.reaches !== undefined) {
						entity.reaches = element.reaches.split(",");
					} else {
						entity.reaches = [];
					}
					entity.reachedBy = [];
					if (entity.antipattern !== false) {
						entity.antipattern = element.antipattern.split(",");
					} else {
						entity.antipattern = [];
					}
					if (entity.roles !== undefined) {
						entity.roles = element.roles.split(",");
					} else {
						entity.roles = [];
					}
					entity.component = element.component;
					entity.version = element.version;
					entity.betweennessCentrality = element.betweennessCentrality;
					entity.changeFrequency = element.changeFrequency;
					if (entity.version !== undefined) {
						if (entitiesByVersion.has(entity.version)) {
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
					if (element.issues !== undefined) {
						entity.issues = element.issues.split(",");
					} else {
						entity.issues = [];
					}
					for (let i = 0; i < entity.issues.length; ++i) {
						entity.issues[i] = entity.issues[i].trim();
					}
					entity.issues.forEach(function (issue) {
						if (entitiesByIssue.has(issue)) {
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
				case "ParameterizableClass":
					entity.superTypes = element.subClassOf.split(",");
					entity.subTypes = element.superClassOf.split(",");
					break;
				case "Attribute":
					if (element.accessedBy) {
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
					path.forEach(function (pathPart) {
						entity.qualifiedName = entity.qualifiedName + pathPart + ".";
					});
					methodSignature.forEach(function (methodSignaturePart) {
						entity.qualifiedName = entity.qualifiedName + methodSignaturePart + " ";
					});

					entity.qualifiedName = entity.qualifiedName.trim();
					if (entity.qualifiedName.slice(-1) == "." ) {
						entity.qualifiedName = element.qualifiedName;
					}

					if (element.calls) {
						entity.calls = element.calls.split(",");
					} else {
						entity.calls = [];
					}
					if (element.calledBy) {
						entity.calledBy = element.calledBy.split(",");
					} else {
						entity.calledBy = [];
					}
					if (element.accesses) {
						entity.accesses = element.accesses.split(",");
					} else {
						entity.accesses = [];
					}

					entity.numberOfStatements = element.number_of_statements;

					break;
				case "Function":
					entity.signature = element.signature;
					entity.qualifiedName = element.qualifiedName;

					if (element.calls) {
						entity.calls = element.calls.split(",");
					} else {
						entity.calls = [];
					}
					if (element.calledBy) {
						entity.calledBy = element.calledBy.split(",");
					} else {
						entity.calledBy = [];
					}
					if (element.accesses) {
						entity.accesses = element.accesses.split(",");
					} else {
						entity.accesses = [];
					}

					entity.dependsOn = element.dependsOn;
					entity.filename = element.filename;

					break;
				case "FunctionModule":
				case "Report":
				case "FormRoutine":
					if (element.calls) {
						entity.calls = element.calls.split(",");
					} else {
						entity.calls = [];
					}
					if (element.calledBy) {
						entity.calledBy = element.calledBy.split(",");
					} else {
						entity.calledBy = [];
					}

					entity.numberOfStatements = element.number_of_statements;

					break;
				case "Variable":
					if (element.accessedBy) {
						entity.accessedBy = element.accessedBy.split(",");
					} else {
						entity.accessedBy = [];
					}

					if (element.declaredType) {
						//if variable is of type array, [] is put after the variable name
						if (element.declaredType.includes("[")) {
							let parts = element.declaredType.split("[");
							entity.displayText = parts[0] + entity.name + "[" + parts[1];
						} else {
							entity.displayText = element.declaredType + " " + element.name;
						}
					} else {
						entity.displayText = element.name;
					}

					entity.dependsOn = element.dependsOn;
					entity.filename = element.filename;

					break;
				case "TranslationUnit":
					entity.filename = element.filename;
					break;
				case "Macro":
					macrosById.set(element.id, entity);
					break;
				case "And":
				case "Or":
					if (element.connected) {
						entity.connected = element.connected.split(",");
						for (let i = 0; i < entity.connected.length; ++i) {
							entity.connected[i] = entity.connected[i].trim();
						}
					} else {
						entity.connected = [];
					}
					break;
				case "Negation":
					entity.negated = element.negated;
					break;
				case "Struct":
				case "Union":
				case "Enum":
				case "EnumValue":
					entity.dependsOn = element.dependsOn;
					entity.filename = element.filename;
					break;
				default:
					break;
			}

			entitiesById.set(element.id, entity);
			newElements.push(entity);
		});

		setReferencesToEntities(newElements);
		return newElements;
	}

	function replaceIdsWithReferences(entity, relationName) {
		const propertiesAsReferences = entity[relationName]
			.filter(id => id && id.length)
			.map(id => [id.trim(), entitiesById.get(id.trim())]);
		entity[relationName] = [];

		propertiesAsReferences.forEach(pair => {
			const [relationTargetId, relationTargetEntity] = pair;
			if (relationTargetEntity === undefined) {
				// no entity matching the id was found - store it to be replaced later
				if (!(relationName in entity.unloadedRelationships)) {
					entity.unloadedRelationships[relationName] = [relationTargetId];
				} else {
					entity.unloadedRelationships[relationName].push(relationTargetId);
				}

				// store the mapping the other way round as well, so we easily know what to replace when we do load that entity
				const entitiesContainingThis = entitiesByContainedUnloadedProperty.get(relationTargetId);
				const referenceReminder = {
					entity: entity,
					property: relationName
				};
				if (entitiesContainingThis) {
					entitiesContainingThis.push(referenceReminder);
				} else {
					entitiesByContainedUnloadedProperty.set(relationTargetId, [referenceReminder]);
				}
			} else {
				entity[relationName].push(relationTargetEntity);
			}
		});
	}

	function setReferencesToEntities(entities) {
		entities.forEach(function (entity) {

			if (entity.belongsTo === undefined || entity.belongsTo === "root") {
				delete entity.belongsTo;
			} else {
				let parent = entitiesById.get(entity.belongsTo);
				if (parent === undefined) {
					events.log.error.publish({ text: "Parent of " + entity.name + " not defined" });
				} else {
					entity.belongsTo = parent;
					parent.children.push(entity);
				}
			}

			switch (entity.type) {
				case "Project":
				case "text":
					break;
				case "issue":
					break;

				case "component":
					replaceIdsWithReferences(entity, 'components');
					break;


				case "Class":
					replaceIdsWithReferences(entity, 'superTypes');
					replaceIdsWithReferences(entity, 'subTypes');

					let reaches = [];
					entity.reaches.forEach(function (reachesId) {
						const relatedEntity = entitiesById.get(reachesId.trim());
						if (relatedEntity !== undefined) {
							reaches.push(relatedEntity);
							relatedEntity.reachedBy.push(entity);
						}
					});
					entity.reaches = reaches;

					replaceIdsWithReferences(entity, 'antipattern');

					entity.roles = entity.roles.map(roleId => roleId.trim());

					break;

				case "ParameterizableClass":
					replaceIdsWithReferences(entity, 'superTypes');
					replaceIdsWithReferences(entity, 'subTypes');
					break;

				case "Attribute":
					replaceIdsWithReferences(entity, 'accessedBy');
					break;

				case "Method":
					replaceIdsWithReferences(entity, 'calls');
					replaceIdsWithReferences(entity, 'calledBy');
					replaceIdsWithReferences(entity, 'accesses');
					break;

				case "Reference":
					replaceIdsWithReferences(entity, 'rcData');
					break;

				case "Function":
					replaceIdsWithReferences(entity, 'calls');
					replaceIdsWithReferences(entity, 'calledBy');
					replaceIdsWithReferences(entity, 'accesses');

					if (entity.dependsOn !== undefined && entity.dependsOn !== "") {
						retrieveAllUsedMacros(entity.dependsOn, entity.id);
					}
					break;

				case "Report":
					if (entity.belongsTo.type == "Namespace") {
						entity.calls = [];
						entity.calledBy = [];
						return;
					}
				case "FunctionModule":
				case "FormRoutine":
					replaceIdsWithReferences(entity, 'calls');
					replaceIdsWithReferences(entity, 'calledBy');
					break;

				case "Variable":
					let variableAccessedBy = [];
					entity.accessedBy.forEach(function (accessedById) {
						let relatedEntity = entitiesById.get(accessedById.trim());
						if (relatedEntity !== undefined && !variableAccessedBy.includes(relatedEntity)) {
							variableAccessedBy.push(relatedEntity);
						}
					});
					entity.accessedBy = variableAccessedBy;

					if (entity.dependsOn !== undefined && entity.dependsOn !== "") {
						retrieveAllUsedMacros(entity.dependsOn, entity.id);
					}
					break;

				case "Struct":
				case "Union":
				case "Enum":
					if (entity.dependsOn !== undefined && entity.dependsOn !== "") {
						retrieveAllUsedMacros(entity.dependsOn, entity.id);
					}
					break;
				default:
					break;
			}

			const entitiesReferencingThis = entitiesByContainedUnloadedProperty.get(entity.id);
			if (entitiesReferencingThis) {
				entitiesReferencingThis.forEach(referenceReminder => {
					const {entity: refEntity, property: refProperty} = referenceReminder;
					// add newly loaded element to property lists it's supposed to be on
					refEntity[refProperty].push(entity);
					// remove it from the list of properties that haven't been loaded yet
					refEntity.unloadedRelationships[refProperty] =
						refEntity.unloadedRelationships[refProperty].filter(id => id !== entity.id);
				});
				entitiesByContainedUnloadedProperty.delete(entity.id);
			}
		});

		//set all parents attribute
		entitiesById.forEach(function (entity) {
			entity.allParents = getAllParentsOfEntity(entity);
		});
	}

	function reset() {
		eventEntityMap.forEach(function (entityMap, eventKey, map) {
			entityMap.forEach(function (entity, entityId) {
				entity[eventKey.name] = false;
			});
			entityMap.clear();
		});
	}



	function createEntity(type, id, name, qualifiedName, belongsTo) {
		let entity = {
			type: type,
			id: id,
			name: name,
			qualifiedName: qualifiedName,
			belongsTo: belongsTo,
			children: [],
			allParents: [],
			unloadedRelationships: {}
		};

		const statesArray = Object.keys(states);
		statesArray.forEach(function (stateName) {
			entity[stateName] = false;
		});

		entitiesById.set(id, entity);

		return entity;
	}

	function removeEntity(id) {
		entitiesById.delete(id);
	}



	function getAllParentsOfEntity(entity) {
		let parents = [];

		if (entity.belongsTo !== undefined && entity.belongsTo !== "") {
			const parent = entity.belongsTo;
			parents.push(parent);

			const parentParents = getAllParentsOfEntity(parent);
			parents = parents.concat(parentParents);
		}

		return parents;
	}

	function getAllChildrenOfEntity(entity) {
		let children = [];

		if (entity.children.length != 0) {
			entity.children.forEach(function(child) {
				children.push(child);
				const grandChildren = getAllChildrenOfEntity(child);
				children = children.concat(grandChildren);
			})
		}

		return children;
	}

	function retrieveAllUsedMacros(conditionId, modelElementId) {
		var conditionEntity = getEntityById(conditionId);

		switch (conditionEntity.type) {
			case "Macro":
				var modelEntity = getEntityById(modelElementId);
				if (modelElementsByMacro.get(conditionEntity.id) === undefined) {
					var modelElements = [];
					modelElements.push(modelEntity);
					modelElementsByMacro.set(conditionEntity.id, modelElements);
				} else {
					var modelElements = modelElementsByMacro.get(conditionEntity.id);
					modelElements.push(modelEntity);
					modelElementsByMacro.set(conditionEntity.id, modelElements);
				}
				break;
			case "And":
			case "Or":
				var connectedElementIds = conditionEntity.connected;
				connectedElementIds.forEach(function (connectedEntityId) {
					retrieveAllUsedMacros(String(connectedEntityId), modelElementId);
				});
				break;
			case "Negation":
				let negatedElementId = conditionEntity.negated;
				retrieveAllUsedMacros(negatedElementId, modelElementId);
				break;
			default:
				break;
		}
	}

	function getAllEntities() {
		return entitiesById;
	}

	function getEntityById(id) {
		return entitiesById.get(id);
	}

	function getIssuesById(id) {
		return issuesById.get(id);
	}

	function getAllVersions() {
		return entitiesByVersion;
	}

	function getAllIssues() {
		return issues;
	}

	function getAllMacrosById() {
		return macrosById;
	}

	function getModelElementsByMacro(id) {
		return modelElementsByMacro.get(id);
	}

	function getAllSecureEntities() {
		let entities = [];
		entitiesById.forEach(function (entity) {
			if (entity.type === "Class" && entity.numberOfOpenSecurityIssues === 0) {
				entities.push(entity);
			}
		});
		return entities;
	}

	function getAllCorrectEntities() {
		let entities = [];
		entitiesById.forEach(function (entity) {
			if (entity.type === "Class" && entity.numberOfOpenIssues === 0 && entity.numberOfOpenSecurityIssues === 0) {
				entities.push(entity);
			}
		});
		return entities;
	}

	function getEntitiesByComponent(component) {
		let entities = [];
		entitiesById.forEach(function (entity) {
			if (entity.component === component) {
				entities.push(entity);
			}
		});
		return entities;
	}

	function getRole(start, pattern) {
		let result = "";
		paths.forEach(function (path) {
			if (start === path.start && path.belongsTo.id === pattern) {
				result = path.role;
			}
		});
		return result;
	}

	function getRoleBetween(start, end) {
		for (let i = 0; i < paths.length; ++i) {
			const path = paths[i];
			if (path.start === start && path.end === end) {
				return path.role;
			}
		}
	}

	function getPaths(start, pattern) {
		let targets = [];
		paths.forEach(function (path) {
			if (start === path.start && path.belongsTo.id === pattern) {
				targets.push((path.end));
			}
		});
		return targets;
	}

	function getEntitiesByAntipattern(antipatternID) {
		let entities = [];
		entitiesById.forEach(function (entity) {
			let antipattern = [];
			if (entity.type === "Class") {
				antipattern = entity.antipattern;
				for (let i = 0; i < antipattern.length; i++) {
					if (antipattern[i].id === antipatternID) {
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

	function getEntitiesByState(stateEventObject) {
		return eventEntityMap.get(stateEventObject);
	}

	function getEntitiesByVersion(versionId) {
		return entitiesByVersion.get(versionId);
	}

	function getEntitiesByIssue(issue) {
		return entitiesByIssue.get(issue);
	}

	function getEntitiesByType(type) {
		let entities = [];
		entitiesById.forEach(function (value) {
			if (value.type === type) {
				entities.push(value)
			}
		});
		return entities;
	}

	function getCodeEntities(type) {
		let entities = [];
		entitiesById.forEach(function (value) {
			if (value.type !== "Negation" && value.type !== "Macro" && value.type !== "And" && value.type !== "Or") {
				entities.push(value)
			}
		});
		return entities;
	}

	function getLabels() {
		return labels;
	}

	function getSelectedVersions() {
		return selectedVersions;
	}

	return {
		initialize: initialize,
		reset: reset,
		states: states,

		getAllEntities: getAllEntities,
		getAllSecureEntities: getAllSecureEntities,
		getAllCorrectEntities: getAllCorrectEntities,
		getEntityById: getEntityById,
		getEntitiesByState: getEntitiesByState,
		getEntitiesByComponent: getEntitiesByComponent,
		getEntitiesByAntipattern: getEntitiesByAntipattern,
		getEntitiesByVersion: getEntitiesByVersion,
		getEntitiesByIssue: getEntitiesByIssue,
		getEntitiesByType: getEntitiesByType,
		getAllParentsOfEntity: getAllParentsOfEntity,
		getAllChildrenOfEntity: getAllChildrenOfEntity,
		getAllVersions: getAllVersions,
		getAllIssues: getAllIssues,
		getIssuesById: getIssuesById,
		getAllMacrosById: getAllMacrosById,
		getModelElementsByMacro: getModelElementsByMacro,
		createEntity: createEntity,
		removeEntity: removeEntity,
		createEntititesFromMetadata: createEntititesFromMetadata,

		addVersion: addVersion,
		removeVersion: removeVersion,
		addIssue: addIssue,
		removeIssue: removeIssue,
		getSelectedVersions: getSelectedVersions,
		getPaths: getPaths,
		getRole: getRole,
		getRoleBetween: getRoleBetween,
		getLabels: getLabels,
		getCodeEntities: getCodeEntities
	};

})();
