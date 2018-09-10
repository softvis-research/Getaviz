class TransformationHelper {
	static resetTransformations(filter) {
		// hide all entities
		events.filtered.on.publish({ sender: filterController, entities: filter.visualizedEntities });

		// make all entities opaque
		canvasManipulator.changeTransparencyOfEntities(filter.visualizedEntities, 0);

		// unselect all entities
		canvasManipulator.unhighlightEntities(filter.visualizedEntities);

		// reset color of relations & remove connectors
		canvasManipulator.resetColorOfEntities(filter.visualizedEntities);
		RelationHelper.removeAddedElements();

		// show all entities when filter is empty
		if (filter.containers.length < 1) {
			events.filtered.off.publish({
				sender: filterController,
				entities: filter.visualizedEntities
			});
		}
	}

	static makeEntitiesVisible(entityIds) {
		if (entityIds.length < 1) return;

		// get entities from entityIds & publish filtered.on event for entities
		let entities = entityIds.map(id => model.getEntityById(id)).filter(entity => entity != null);
		events.filtered.off.publish({ sender: filterController, entities: entities });
	}

	static makeEntitiesInvisible(entityIds) {
		if (entityIds.length < 1) return;

		// get entities from entityIds & publish filtered.on event for entities
		let entities = entityIds.map(id => model.getEntityById(id)).filter(entity => entity != null);
		events.filtered.on.publish({ sender: filterController, entities: entities });
	}

	static makeEntitiesOpaque(entityIds) {
		if (entityIds.length < 1) return;

		// get entities from entityIds & make requested entities opaque
		let entities = entityIds.map(id => model.getEntityById(id)).filter(entity => entity != null);
		canvasManipulator.changeTransparencyOfEntities(entities, 0);
	}

	static makeEntitiesTransparent(entityIds) {
		if (entityIds.length < 1) return;

		// get entities from entityIds & make requested entities translucent
		let entities = entityIds.map(id => model.getEntityById(id)).filter(entity => entity != null);
		canvasManipulator.changeTransparencyOfEntities(entities, 0.85);
	}

	static selectEntities(entityIds) {
		if (entityIds.length < 1) return;

		// get entities from entityIds & highlight requested entities
		let entities = entityIds.map(id => model.getEntityById(id)).filter(entity => entity != null);
		canvasManipulator.highlightEntities(entities, canvasManipulator.colors.darkred);
	}

	static connectEntities(entityIds, loadedPositions) {
		if (entityIds.length < 1) return;

		let fields = ['subTypes', 'superTypes', 'accessedBy', 'accesses', 'calls', 'calledBy'];
		let color = '0 0.5 0.5';
		let size = 0.5;
		let skipCounter = 0;
		let relationParticipants = [];

		// remove connectors
		RelationHelper.removeAddedElements();

		// prepare container to add all connectors to before adding it to the DOM
		let transformContainer = document.createElement('Transform');
		transformContainer.className = 'transformContainer';

		// get entities from entityIds
		let entities = entityIds.map(id => model.getEntityById(id)).filter(entity => entity != null);

		console.time('calculating relation connectors');

		// get relations for each entity of selection
		entities.forEach(entity => {
			let relations = [];

			// collect entities that are related to entity of selection
			fields.forEach(field => {
				if (entity[field]) relations = relations.concat(entity[field]);
			});

			// remove entities that aren't in selection
			relations = relations.filter(entity => entityIds.indexOf(entity.id) > -1);

			// add entities that take part in a relation for later
			if (relations.length > 0) {
				relationParticipants.push(entity);
				relationParticipants = relationParticipants.concat(relations.map(relation => relation.id));
			}

			// calculate connector for each relation
			relations.forEach(relation => {
				let start = RelationHelper.getObjectPosition(entity.id, loadedPositions);
				let end = RelationHelper.getObjectPosition(relation.id, loadedPositions);

				// skip connector where either the entity or related entity doesn't have position metadata
				if (start == null || end == null) {
					skipCounter = skipCounter + 1;
				} else {
					// add connector to transform container
					let transform = RelationHelper.createLine(start, end, color, size);
					transform.className = 'expansionLine';
					transformContainer.appendChild(transform);
				}
			});
		});
		console.timeEnd('calculating relation connectors');

		if (skipCounter > 0)
			console.log('Skipped ' + skipCounter + ' relations because of them lacking position data.');

		// remove duplicates
		relationParticipants = relationParticipants.filter((entity, index) => {
			return relationParticipants.indexOf(entity) === index;
		});

		// make all entities visible that take part in a relation
		TransformationHelper.makeEntitiesVisible(relationParticipants);

		// color relations
		canvasManipulator.resetColorOfEntities(relationParticipants);
		canvasManipulator.changeColorOfEntities(relationParticipants, color);

		// add transform container to DOM
		canvasManipulator.addElement(transformContainer);
	}
}
