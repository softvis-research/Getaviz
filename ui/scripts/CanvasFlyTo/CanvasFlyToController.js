var canvasFlyToController = (function() {
    
	//config parameters	
	const controllerConfig = {
		parentLevel: 0,
		targetType: "any"
	};
	
	
	function initialize(setupConfig){
		application.transferConfigParams(setupConfig, controllerConfig);
		
    }
	
	function activate(){	
		events.selected.on.subscribe(onEntitySelected);
		events.componentSelected.on.subscribe(onComponentSelected);
		events.antipattern.on.subscribe(onAntipatternSelected);
        events.versionSelected.on.subscribe(onVersionSelected);
	}

	function deactivate(){
		events.selected.on.unsubscribe(onEntitySelected);
		events.componentSelected.on.unsubscribe(onComponentSelected);
		events.antipattern.on.unsubscribe(onAntipatternSelected);
        events.versionSelected.on.unsubscribe(onVersionSelected);
	}
	
	function onVersionSelected(applicationEvent) {
		const version = applicationEvent.entities[0];
		const entities = model.getEntitiesByVersion(version);
		let namespace = entities[0];
		let found = false;
		
		for(let i = 0; i < entities.length && found === false; ++i) {
			if(entities[i].type === "Namespace") {
				const count = (entities[i].qualifiedName.match(/\./g) || []).length;
				if(count === 0) {
					namespace = entities[i];
					found = true;
				}
			}
		}
		canvasManipulator.flyToEntity(namespace);
	}
        
    function onAntipatternSelected(applicationEvent) {
        const entity = applicationEvent.entities[0];
        const classes = model.getEntitiesByAntipattern(entity.id);
        const parents = new Map();
        for(let i = 0; i < classes.length; i++) {
            const cParents = model.getAllParentsOfEntity(classes[i]);
            parents.set(classes[i].id, 1);
            for(let j = 0; j < cParents.length; j++) {
                if(parents.has(cParents[j].id)){
                    const value = parents.get(cParents[j].id);
                    parents.set(cParents[j].id, value +1);
                } else {
                    parents.set(cParents[j].id, 1);
                }
            }
        }
        let result = "";

        let max = 0;
        parents.forEach(function (key, value) {
            if(key > max) {
                result = value;
                max = key;
            }
        });
        const final = model.getEntityById(result);
        canvasManipulator.flyToEntity(final);
    }
	
	function onComponentSelected(applicationEvent) {
		const entity = applicationEvent.entities[0];
		const classes = model.getEntitiesByComponent(entity.id);

		const parents = new Map();
		for(let i = 0; i < classes.length; i++) {
			const cParents = model.getAllParentsOfEntity(classes[i]);
			parents.set(classes[i].id, 1);
			for(let j = 0; j < cParents.length; j++) {
				if(parents.has(cParents[j].id)){
					const value = parents.get(cParents[j].id);
					parents.set(cParents[j].id, value +1);
				} else {
					parents.set(cParents[j].id, 1);
				}
			}
		}
		let result = "";

		let max = 0;
		parents.forEach(function (key, value) {
			if(key > max) {
				result = value;
				max = key;
			}
		});
		const final = model.getEntityById(result);
		canvasManipulator.flyToEntity(final);
    }
	
	function onEntitySelected(applicationEvent) {		
		let entity = applicationEvent.entities[0];
		let parent;
		let parentLevel = 0;
		while(parentLevel < controllerConfig.parentLevel){
			parent = entity.belongsTo;
			if(parent === undefined){
				break;
			}
			entity = parent;
			parentLevel = parentLevel + 1;
		}
		
		if(controllerConfig.targetType === "any") {
			canvasManipulator.flyToEntity(entity);
        } else {
			flyToParentEntity(entity);
        }
	}
	
	function flyToParentEntity(entity) {
		if(entity.type === controllerConfig.targetType) {
			canvasManipulator.flyToEntity(entity);
        } else {
            flyToParentEntity(entity.belongsTo);
        }
    }

    return {
        initialize: initialize,
		activate:	activate,
		deactivate: deactivate
    };    
})();
