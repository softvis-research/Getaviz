var relationController = function () {

	// list of entities whose relations to others are being displayed (not including intermediate steps of recursive relations)
	let sourceEntities = new Array();
	// for every source entity (and intermediate of recursive relations), the list of entities it is related to
	let relatedEntitiesMap = new Map();
	// set of all entities that are related to sourceEntities overall, not including the source entities themselves
	let relatedEntitiesSet = new Set();

	let connectors = new Array();
	let relations = new Array();

	let activated = false;

	let relationConnectionHelper;

	//config parameters
	const controllerConfig = {
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
		relationConnectionHelper = createRelationConnectionHelper(controllerConfig);

		events.selected.on.subscribe(onRelationsChanged);
		events.selected.off.subscribe(onEntityDeselected);
		events.filtered.on.subscribe(onEntityFiltered);
		events.filtered.off.subscribe(onEntityUnfiltered);
	}

	async function activate() {
		activated = true;
		await unhideRelatedEntities();

		if (relatedEntitiesMap.size != 0) {
			if (controllerConfig.showConnector) {
				createRelatedConnections(relations);
			}
			if (controllerConfig.showHighlight) {
				highlightRelatedEntities(relatedEntitiesSet);
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
			unhighlightAllRelatedEntities();
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

	async function onEntityUnfiltered(applicationEvent) {
		// do not needlessly trigger on unfiltering caused by relations
		if (sourceEntities.length && applicationEvent.sender !== relationController) {
			const newRelations = await loadAllRelationsTo(applicationEvent.entities);
			if (!activated) return;

			await canvasManipulator.waitForRenderOfElement(applicationEvent.entities[0]);

			if (controllerConfig.showHighlight) {
				const newRelatedEntities = new Set(newRelations.map(entity => entity.target));
				highlightRelatedEntities(newRelatedEntities);
			}
			if (controllerConfig.showConnector) {
				createRelatedConnections(newRelations);
			}
		}
	}

	// remove all relations involving these entities from the internal relation state
	// this does not remove these entities from the target lists of relatedEntitiesMap, that already happens earlier in onEntityFiltered
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

		if (controllerConfig.useMultiSelect) {
			sourceEntities = applicationEvent.entities;
		} else {
			sourceEntities.push(applicationEvent.entities[0]);
		}

		events.log.info.publish({ text: "connector - onRelationsChanged - selected Entity - " + applicationEvent.entities[0] });

		await loadAllRelationsOf(sourceEntities);

		if (controllerConfig.showRecursiveRelations) {
			await loadAllRecursiveRelationsOf(sourceEntities);
		}

		events.log.info.publish({ text: "connector - onRelationsChanged - related Entities - " + relatedEntitiesMap.size });

		if (relatedEntitiesMap.size == 0) {
			return;
		}

		if (activated) {
			if (controllerConfig.showHighlight) {
				highlightRelatedEntities(relatedEntitiesSet);
			}

			await unhideRelatedEntities();

			if (controllerConfig.showConnector) {
				createRelatedConnections(relations);
			}
		}
	}

	// given a list of entities, return a map depicting all relations originating from them
	async function getRelatedEntities(sourceEntitiesArray) {
		const relatedEntities = new Map();
		let entitiesToLoad = [];
		for (const sourceEntity of sourceEntitiesArray) {
			const unloadedRelatedEntities = getRelatedEntitiesOfSourceEntity(sourceEntity.unloadedRelationships, sourceEntity.type);
			if (unloadedRelatedEntities.length) {
				entitiesToLoad = entitiesToLoad.concat(unloadedRelatedEntities);
			}
		}
		if (entitiesToLoad.length) {
			await neo4jModelLoadController.loadTreesContainingAnyOf(entitiesToLoad);
		}
		for (const sourceEntity of sourceEntitiesArray) {
			relatedEntities.set(sourceEntity, getRelatedEntitiesOfSourceEntity(sourceEntity, sourceEntity.type));
		}
		return relatedEntities;
	}

	// add these new relations to the internal relation state - duplicates will be filtered
	function loadRelations(newRelationMap) {
		const newRelations = [];
		for (const [sourceEntity, allRelatedEntitiesOfSourceEntity] of newRelationMap) {
			const oldRelatedEntities = relatedEntitiesMap.get(sourceEntity);
			const relatedEntitiesOfSourceEntity = new Set(oldRelatedEntities);

			for (const relatedEntity of allRelatedEntitiesOfSourceEntity) {
				if (relatedEntitiesOfSourceEntity.has(relatedEntity)) {
					events.log.info.publish({ text: "connector - onRelationsChanged - multiple relation" });
					break;
				}
				if (!controllerConfig.showInnerRelations) {
					if (isTargetChildOfSourceParent(relatedEntity, sourceEntity)) {
						events.log.info.publish({ text: "connector - onRelationsChanged - inner relation" });
						break;
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
				newRelations.push(relation);

				relatedEntitiesOfSourceEntity.add(relatedEntity);
				relatedEntitiesSet.add(relatedEntity);
			}

			relatedEntitiesMap.set(sourceEntity, Array.from(relatedEntitiesOfSourceEntity));
		}

		return newRelations;
	}

	async function loadAllRelationsOf(sourceEntitiesArray) {
		const newRelatedEntities = await getRelatedEntities(sourceEntitiesArray);
		return loadRelations(newRelatedEntities);
	}

	async function loadAllRelationsTo(targetEntitiesArray) {
		// there is no guarantee that all relations have a matching inverse
		// so we'll have to re-find all relations and filter them down to the ones pointing at the targets
		const newRelatedEntities = await getRelatedEntities(sourceEntities);
		const targetEntitiesSet = new Set(targetEntitiesArray);
		for (const [source, targets] of newRelatedEntities) {
			newRelatedEntities.set(source, targets.filter(target => targetEntitiesSet.has(target)));
		}
		return loadRelations(newRelatedEntities);
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
			case "Transaction":	
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

	async function loadAllRecursiveRelationsOf(oldSourceEntities) {
		for (const oldSourceEntity of oldSourceEntities) {
			const relatedEntities = relatedEntitiesMap.get(oldSourceEntity);

			if (relatedEntities.length == 0) {
				return;
			}

			newSourceEntities = relatedEntities.filter(relatedEntity => (!relatedEntitiesMap.has(relatedEntity)));

			if (newSourceEntities.length == 0) {
				return;
			}

			await loadAllRelationsOf(newSourceEntities);
			await loadAllRecursiveRelationsOf(newSourceEntities);
		}
	}


	/*************************
			Connection
	*************************/

	function createRelatedConnections(newRelations) {

		newRelations.forEach(function (relation) {
			const sourceEntity = relation.source;
			const relatedEntity = relation.target;

			//create scene element
			const connectorElements = relationConnectionHelper.createConnector(sourceEntity, relatedEntity, relation.id);

			//source or target not rendered -> no connector
			if (!connectorElements) {
				events.log.error.publish({ text: "connector - createRelatedConnections - source or target not rendered" });
				return;
			}

			events.log.info.publish({ text: "connector - createRelatedConnections - create connector" });

			connectorElements.forEach(function (element) {
				connectors.push(element);
			});
		})
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


	/************************
			Highlight
	************************/

	function highlightRelatedEntities(newRelatedEntities) {
		if (newRelatedEntities.size == 0) {
			return;
		}

		const visibleEntities = Array.from(newRelatedEntities).filter(entity => !entity.filtered);
		canvasManipulator.highlightEntities(visibleEntities, controllerConfig.highlightColor, { name: "relationController" });
	}

	function unhighlightAllRelatedEntities() {
		if (relatedEntitiesSet.size == 0) {
			return;
		}

		const visibleEntities = Array.from(relatedEntitiesSet).filter(entity => !entity.filtered);
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
