var canvasFlyToController = (function() {
    
	//config parameters	
	var controllerConfig = {
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
		//events.versionSelected.off.subscribe(offVersionSelected);
	}

	function deactivate(){
		events.selected.on.unsubscribe(onEntitySelected);
		events.componentSelected.on.unsubscribe(onComponentSelected);
		events.antipattern.on.unsubscribe(onAntipatternSelected);
        events.versionSelected.on.unsubscribe(onVersionSelected);
		//events.versionSelected.off.unsubscribe(offVersionSelected);
	}
	
	function onVersionSelected(applicationEvent) {
		var version = applicationEvent.entities[0];
		var entities = model.getEntitiesByVersion(version);
		var namespace = entities[0];
		var found = false;
		
		for(var i = 0; i < entities.length && found == false; ++i) {
			if(entities[i].type == "Namespace") {
				var count = (entities[i].qualifiedName.match(/\./g) || []).length;
				if(count == 0) {
					namespace = entities[i];
					found = true;
				}
			}
		}
		canvasManipulator.flyToEntity(namespace);
	}
        
    function onAntipatternSelected(applicationEvent) {
        var entity = applicationEvent.entities[0];
        var classes = model.getEntitiesByAntipattern(entity.id);
        var parents = new Map();
        for(var i = 0; i < classes.length; i++) {
            var cparents = model.getAllParentsOfEntity(classes[i]);
            parents.set(classes[i].id, 1);
            for(var j = 0; j < cparents.length; j++) {
                if(parents.has(cparents[j].id)){
                    var value = parents.get(cparents[j].id);
                    parents.set(cparents[j].id, value +1);
                } else {
                    parents.set(cparents[j].id, 1);
                }
            }
        }
        var result = "";

        var max = 0;
        parents.forEach(function (key, value) {
            if(key > max) {
                result = value;
                max = key;
                console.log(value)
				console.log(key)
            }
        });
        var final = model.getEntityById(result);
        console.log(final.id)
		console.log(final.qualifiedName)
        canvasManipulator.flyToEntity(final);
    }
	
	function onComponentSelected(applicationEvent) {
		var entity = applicationEvent.entities[0];
		var classes = model.getEntitiesByComponent(entity.id);

		var parents = new Map();
		for(var i = 0; i < classes.length; i++) {
			var cparents = model.getAllParentsOfEntity(classes[i]);
			parents.set(classes[i].id, 1);
			for(var j = 0; j < cparents.length; j++) {
				if(parents.has(cparents[j].id)){
					var value = parents.get(cparents[j].id);
					parents.set(cparents[j].id, value +1);
				} else {
					parents.set(cparents[j].id, 1);
				}
			}
		}
		var result = "";

		var max = 0;
		parents.forEach(function (key, value) {
			if(key > max) {
				result = value;
				max = key;
			}
		});
		var final = model.getEntityById(result);
		canvasManipulator.flyToEntity(final);
    }
	
	function onEntitySelected(applicationEvent) {		
		var entity = applicationEvent.entities[0];
		var parent;				
		var parentLevel = 0;
		while(parentLevel < controllerConfig.parentLevel){
			parent = entity.belongsTo;
			if(parent == undefined){
				break;
			}
			entity = parent;
			parentLevel = parentLevel + 1;
		}
		
		if(controllerConfig.targetType == "any") {
			canvasManipulator.flyToEntity(entity);
        } else {
			flyToParentEntity(entity);
        }
	}
	
	function flyToParentEntity(entity) {
		if(entity.type == controllerConfig.targetType) {
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
