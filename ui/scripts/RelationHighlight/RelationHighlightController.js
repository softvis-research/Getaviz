var relationHighlightController = function(){
		
	var relatedEntities = new Array();
	var activated = false;
	
	function initialize(config){		
		events.selected.on.subscribe(onRelationsChanged);
	}
	
	function activate(){	
		
		activated = true;
		if(relatedEntities.length != 0){
			highlightRelatedEntities();
		}
	}

	function deactivate(){	
		reset();
		activated = false;
	}
	
	function reset(){
		canvasManipulator.resetColorOfEntities(relatedEntities);
	}
	
	
	function resetColor(){
		if(relatedEntities.length == 0){	
			return;
		}

		var relatedEntitesMap = new Map();
		
		//highlight related entities
		relatedEntities.forEach(function(relatedEntity){		
			if(relatedEntity.marked){
				return;
			}
			
			if(relatedEntitesMap.has(relatedEntity)){
				return;
			}

			relatedEntitesMap.set(relatedEntity, relatedEntity);
		});

		canvasManipulator.resetColorOfEntities(Array.from(relatedEntitesMap.keys()));
	}
		
	
	function onRelationsChanged(applicationEvent) {
		
		resetColor();
		
		
		//get related entites
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
		
		if(activated){
			highlightRelatedEntities();
		}
		
	}

	function highlightRelatedEntities(){
		var relatedEntitesMap = new Map();
		
		//highlight related entities
		relatedEntities.forEach(function(relatedEntity){		
			if(relatedEntity.marked){
				return;
			}
			
			if(relatedEntitesMap.has(relatedEntity)){
				return;
			}

			relatedEntitesMap.set(relatedEntity, relatedEntity);
		});
			
		canvasManipulator.changeColorOfEntities(Array.from(relatedEntitesMap.keys()), "0 0 0");			
	}

		

	return {
        initialize	: initialize,
		reset		: reset,
		activate	: activate,
		deactivate	: deactivate
    };    

}();