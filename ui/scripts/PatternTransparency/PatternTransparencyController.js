var patternTransparencyController = (function() {

	//TODO Refactoring der Massenänderungen -> Generelle Umstellung auf ArrayVerarbeitung
	
	var relatedEntities = [];
	var parents = [];

	var activated = false;
	var faded = false;
	
	//config parameters	
	var controllerConfig = {
		fullFadeValue : 0.85,
		noFadeValue : 0.0,
		startFaded: false,
	};
	var lastApplicationEvent = null;
	
	function initialize(setupConfig){
        application.transferConfigParams(setupConfig, controllerConfig);	
        events.componentSelected.on.subscribe(onComponentSelected);
        events.antipattern.on.subscribe(onAntipatternSelected);
		events.selected.on.subscribe(onRelationsChanged); 
    }
	
	function activate(){
		activated = true;	
		
		if(controllerConfig.startFaded){
			setTimeout(fadeAll, 1000);
		}

		if(relatedEntities.length !== 0){
			fadeEntities();
		}
	}

	function deactivate(){
		reset();
		activated = false;
	}

	function reset(){
		if(faded){
			setTimeout(unfadeAll, 1000);							
		}
		faded = false;
	}
	
	function onRelationsChanged(applicationEvent) {
		relatedEntities = [];
		if(lastApplicationEvent == null) {
			unfadeAll();
			addReachesAndReachedBy(applicationEvent.entities[0]);
			//get parents of releated entities
			parents = [];
			relatedEntities.forEach(function(relatedEntity){
				parents = parents.concat(relatedEntity.allParents);
				relatedEntity.isTransparent = false;
			});

            relatedEntities = relatedEntities.concat(model.getLabels());

            parents.forEach(function(parent){
				parent.isTransparent = false;
			});
			if(activated){
				fadeEntities();
			}
			relatedEntities.forEach(function(relatedEntity){
				relatedEntity.isTransparent = false;
			});
		} else {
			if(lastApplicationEvent.entities[0].id !== applicationEvent.entities[0].component) {
				unfadeAll();
				lastApplicationEvent = null;
			}
		}
    } 

	function unfadeAll(){
        var entities = model.getAllEntities();
		canvasManipulator.changeTransparencyOfEntities(entities, controllerConfig.noFadeValue);
        canvasManipulator.resetColorOfEntities(entities);
        faded = false;
		model.getAllEntities().forEach(function(entity){
			entity.isTransparent = false;
		});
    }
	
	function addReachesAndReachedBy (entity) {
		relatedEntities.push(entity);
		entity.reaches.forEach(function(element) {
			relatedEntities.push(element);
		});  
		entity.reachedBy.forEach(function(element){
			if(relatedEntities.includes(element)){
			} else {
				relatedEntities.push(element);
			}
		});
	}

    function onAntipatternSelected(applicationEvent) {
        lastApplicationEvent = applicationEvent;
        unfadeAll();

        //get new related entites
        var entity = applicationEvent.entities[0];

        switch(entity.type) {
            case "component":
                relatedEntities = model.getEntitiesByComponent(entity.id);
                var components = entity.components;
                for(var i = 0; i < components.length; ++i) {
                    relatedEntities = relatedEntities.concat(model.getEntitiesByComponent(components[i].id));
                }
                break;
            case "stk":
                relatedEntities = model.getEntitiesByAntipattern(entity.id);
        }

        relatedEntities = relatedEntities.concat(model.getLabels());

        if(relatedEntities.length === 0){
            return;
        }

        //get parents of releated entities
        parents = [];
        relatedEntities.forEach(function(relatedEntity){
            parents = parents.concat(relatedEntity.allParents);
        });

        if(activated){
            fadeEntities();
            parents.forEach(function(parent){
                parent.isTransparent = false;
            });
            relatedEntities.forEach(function(relatedEntity){
                relatedEntity.isTransparent = false;
            });
        }
    }
	
	function onComponentSelected(applicationEvent) {
		lastApplicationEvent = applicationEvent;
        unfadeAll();

		//get new related entites
		var entity = applicationEvent.entities[0];	
		
		switch(entity.type) {
			case "component":
				relatedEntities = model.getEntitiesByComponent(entity.id);
				var components = entity.components;
				for(var i = 0; i < components.length; ++i) {
					relatedEntities = relatedEntities.concat(model.getEntitiesByComponent(components[i].id));
				}
				break;
			case "stk":
				relatedEntities = model.getEntitiesByAntipattern(entity.id);
		}

        if(relatedEntities.length === 0){
            return;
		}

        relatedEntities = relatedEntities.concat(model.getLabels());


        //get parents of releated entities
		parents = [];
		relatedEntities.forEach(function(relatedEntity){
			parents = parents.concat(relatedEntity.allParents);
		});

		if(activated){
			fadeEntities();
            parents.forEach(function(parent){
                parent.isTransparent = false;
            });
			relatedEntities.forEach(function(relatedEntity){
				relatedEntity.isTransparent = false;
			});
		}
    }

	function fadeEntities(){
		//first relation selected -> fade all entities
		fadeAll();

		//unfade parents of related entities
        canvasManipulator.resetColorOfEntities(parents);
        canvasManipulator.changeTransparencyOfEntities(parents, controllerConfig.noFadeValue);
		
        //unfade related entities
        canvasManipulator.resetColorOfEntities(relatedEntities);
        canvasManipulator.changeTransparencyOfEntities(relatedEntities, controllerConfig.noFadeValue);
	}

	function fadeAll(){
		if(!faded) {
            var entities = model.getAllEntities();
            canvasManipulator.changeColorOfEntities(entities, "white");
            canvasManipulator.changeTransparencyOfEntities(entities, controllerConfig.fullFadeValue);
            faded = true;
            model.getAllEntities().forEach(function (entity) {
                entity.isTransparent = true;
            });
        }
	}
	
	 return {
        initialize: 	initialize,
		activate: 		activate,
		deactivate:		deactivate,
		reset: 			reset
        };    
})();
	
    
