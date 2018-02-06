var patternTransparencyController = (function() {

	//TODO Refactoring der Massenänderungen -> Generelle Umstellung auf ArrayVerarbeitung
	
	var relatedEntities = [];
	var parents = [];

	var activated = false;
	var faded = false;
	
	//config parameters	
	var controllerConfig = {
		fullFadeValue : 0.85,
		halfFadeValue : 0.5,
		noFadeValue : 0.0,
		startFaded: false,
	};
	var lastApplicationEvent = null;
	
	function initialize(setupConfig){
        application.transferConfigParams(setupConfig, controllerConfig);	
        events.componentSelected.on.subscribe(onComponentSelected);
		events.selected.on.subscribe(onRelationsChanged); 
    }
	
	function activate(){
		activated = true;	
		
		if(controllerConfig.startFaded){
			setTimeout(fadeAll, 1000);
		}

		if(relatedEntities.length != 0){
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
			addReaches(applicationEvent.entities[0]);
			//get parents of releated entities
			parents = [];
			relatedEntities.forEach(function(relatedEntity){
				parents = parents.concat(relatedEntity.allParents);
				relatedEntity.isTransparent = false;
			});
	
			parents.forEach(function(parent){
				parent.isTransparent = true;
			});
			if(activated){
				fadeEntities();
			}
		} else {
			if(lastApplicationEvent.entities[0].id != applicationEvent.entities[0].component) {
				unfadeAll();
				lastApplicationEvent = null;
			}
		}
    } 

	function unfadeAll(){
		//TODO FIX
		//realy realy bad fix for one model where elements in scene but not in model...
		//add an all elements functionality for canvasmanipulator anyway 
		var allCanvasElementIds = canvasManipulator.getElementIds();
		var allCanvasObjects = [];
		allCanvasElementIds.forEach(function(canvasElementId){
			allCanvasObjects.push({id:canvasElementId});
		});
		canvasManipulator.changeTransparencyOfEntities(allCanvasObjects, controllerConfig.noFadeValue);	
        faded = false;
		model.getAllEntities().forEach(function(entity){
			entity.isTransparent = false;
		});
	}
	
	function addReaches (entity) {
		relatedEntities.push(entity);
		entity.reaches.forEach(function(element) {
			relatedEntities.push(element);
		});           
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
      
		if(relatedEntities.length == 0){
            return;
		}

		//get parents of releated entities
		parents = [];
		console.log("a")
		relatedEntities.forEach(function(relatedEntity){
			console.log("b")
			parents = parents.concat(relatedEntity.allParents);
			relatedEntity.isTransparent = false;
		});

		parents.forEach(function(parent){
			parent.isTransparent = true;
		});
		if(activated){
			fadeEntities();
		}
    }

	function fadeEntities(){
		//first relation selected -> fade all entities				
		fadeAll();

		//unfade parents of related entities				
		canvasManipulator.changeTransparencyOfEntities(parents, controllerConfig.halfFadeValue);
		
			//unfade related entities
		canvasManipulator.changeTransparencyOfEntities(relatedEntities, controllerConfig.noFadeValue);
	}

	function fadeAll(){
		if(!faded){
			
			//TODO FIX
			//realy realy bad fix for one model where elements in scene but not in model...
			//add an all elements functionality for canvasmanipulator anyway 
			var allCanvasElementIds = canvasManipulator.getElementIds();
			var allCanvasObjects = [];
			allCanvasElementIds.forEach(function(canvasElementId){
				allCanvasObjects.push({id:canvasElementId});
			});


			canvasManipulator.changeTransparencyOfEntities(allCanvasObjects, controllerConfig.fullFadeValue);
			faded = true;
			model.getAllEntities().forEach(function(entity){
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
	
    
