class FilterHelper {
	// adds mocked relations to entities
	static injectTestRelations() {
		// extract up to 100 method-entities
		let someMethods = Array.from(model.getAllEntities().values())
			.filter(entity => entity.type == 'Method')
			.slice(0, 100);

		for (let entity of model.getAllEntities().values()) {
			if (!entity.calls) entity.calls = [];

			if (!entity.calledBy) entity.calledBy = [];

			if (entity.calls.concat(entity.calledBy).length > 0) continue;

			// insert random method-calls
			for (let i = 0; i < Math.floor(Math.random() * 2); i++)
				entity.calledBy.push(someMethods[Math.floor(Math.random() * someMethods.length)]);
			for (let i = 0; i < Math.floor(Math.random() * 2); i++)
				entity.calls.push(someMethods[Math.floor(Math.random() * someMethods.length)]);
		}
	}

	// returns Ids of entities that are not in the tMap.get(tKey)-Array
	static convertAlias(tMap, tKey) {
		return Array.from(model.getAllEntities().values())
			.filter(
				ent =>
					tMap
						.get(tKey)
						.map(id => model.getEntityById(id))
						.indexOf(ent) < 0
			)
			.map(ent => ent.id);
	}

	// gets all children recursivly
	static getAllChildren(parent, result) {
		for (let child of parent.children) {
			result.push(child);
			FilterHelper.getAllChildren(child, result);
		}
	}

	// returns entity if FQN of an entity matches query
	static getEntityFromQN(query) {
		for (let entity of model.getAllEntities().values()) {
			if (entity.qualifiedName == query) return entity;
		}

		return null;
	}

	// only executes last "func" call in the last "interval" seconds
	static debounce(func, interval) {
		let lastCall = -1;
		return function() {
			clearTimeout(lastCall);
			let args = arguments;
			lastCall = setTimeout(function() {
				func.apply(this, args);
			}, interval);
		};
	}
}
