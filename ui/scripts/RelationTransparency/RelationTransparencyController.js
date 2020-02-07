var relationTransparencyController = (function() {
	
	var relatedEntities = new Array();
	var parents = new Array();

	var activated = false;
	var faded = false;
	
	//config parameters	
	var controllerConfig = {
		fullFadeValue : 0.85,
		halfFadeValue : 0.55,
		noFadeValue : 0,
		startFaded: false
	};
		
		
		
	
	
	function initialize(setupConfig){	
		
		application.transferConfigParams(setupConfig, controllerConfig);	

		

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

	
	function unfadeAll(){
		//realy realy bad fix for one model where elements in scene but not in model...
		//add an all elements functionality for canvasmanipulator anyway 
		var allCanvasElementIds = canvasManipulator.getElementIds();
		var allCanvasObjects = [];
		allCanvasElementIds.forEach(function(canvasElementId){
			allCanvasObjects.push({id:canvasElementId});
		});

		canvasManipulator.changeTransparencyOfEntities(allCanvasObjects, controllerConfig.noFadeValue);	
	}
	
	
	
	function onRelationsChanged(applicationEvent) {
		
		//fade old related entities and parents
		if(activated){	
			if(relatedEntities.length != 0){				
				canvasManipulator.changeTransparencyOfEntities(relatedEntities, controllerConfig.fullFadeValue);		
			}			
			if(parents.length != 0){			
				canvasManipulator.changeTransparencyOfEntities(parents, controllerConfig.fullFadeValue);						
			}	
		}


		//get new related entities
		var entity = applicationEvent.entities[0];	
		
		relatedEntities = new Array();

		switch(entity.type) {
			case "Class":
				relatedEntities = relatedEntities.concat(entity.superTypes);
				relatedEntities = relatedEntities.concat(entity.subTypes);
				break;
			case  "ParameterizableClass":
				relatedEntities = relatedEntities.concat(entity.superTypes);
				relatedEntities = relatedEntities.concat(entity.subTypes);
				break;			
			case "Attribute":
				relatedEntities = entity.accessedBy;
				break;
			case "Method":
			case "Function":
				relatedEntities = entity.accesses;
				relatedEntities = relatedEntities.concat( entity.calls );
				relatedEntities = relatedEntities.concat( entity.calledBy );			
				break;
			
			default: 				
				return;
		}
		
		if(relatedEntities.length == 0){
			return;
		}

		//get parents of releated entities
		parents = new Array();
		relatedEntities.forEach(function(relatedEntity){
			parents = parents.concat(relatedEntity.allParents);
		});



		if(activated){
			fadeEntities();
		}
		
    }	

	

	function fadeEntities(){
		
		//first relation selected -> fade all entities				
		fadeAll();
						
		//unfade related entities
		canvasManipulator.changeTransparencyOfEntities(relatedEntities, controllerConfig.noFadeValue);
				
		//unfade parents of related entities				
		canvasManipulator.changeTransparencyOfEntities(parents, controllerConfig.halfFadeValue);
	}

	function fadeAll(){
		if(!faded){			
			//realy realy bad fix for one model where elements in scene but not in model...
			//add an all elements functionality for canvasmanipulator anyway 
			var allCanvasElementIds = canvasManipulator.getElementIds();
			var allCanvasObjects = [];
			allCanvasElementIds.forEach(function(canvasElementId){
				allCanvasObjects.push({id:canvasElementId});
			});


			canvasManipulator.changeTransparencyOfEntities(allCanvasObjects, controllerConfig.fullFadeValue);
			faded = true;
		}
	}
	
	
	 return {
        initialize: 	initialize,
		activate: 		activate,
		deactivate:		deactivate,
		reset: 			reset
    };    
})();
	
    