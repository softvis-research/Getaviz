var canvasFlyToController = (function() {
    
	//config parameters	
	var controllerConfig = {
		parentLevel: 0
	}
	
	
	function initialize(setupConfig){	

		application.transferConfigParams(setupConfig, controllerConfig);
		
    }
	
	function activate(){	
		events.selected.on.subscribe(onEntitySelected);		
	}

	function deactivate(){
		events.selected.on.unsubscribe(onEntitySelected);
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
		
		canvasManipulator.flyToEntity(entity);
	}	

    return {
        initialize: initialize,
		activate:	activate,
		deactivate: deactivate
    };    
})();