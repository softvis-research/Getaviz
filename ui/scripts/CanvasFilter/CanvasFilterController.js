var canvasFilterController = (function() {
    
    function initialize(){
    }
	
	function activate(){
		events.filtered.on.subscribe(onEntityFilter);
		events.filtered.off.subscribe(onEntityUnfilter);
		events.versionSelected.on.subscribe(onVersionSelected);
		events.versionSelected.off.subscribe(offVersionSelected);
    }
		
	function onEntityFilter(applicationEvent) {				
		var entities = applicationEvent.entities;
		canvasManipulator.hideEntities(entities);

	}
	
	function onEntityUnfilter(applicationEvent) {
		var entities = applicationEvent.entities;
		canvasManipulator.showEntities(entities);
	}
	
	function onVersionSelected(applicationEvent) {
        var entities = model.getEntitiesByVersion(applicationEvent.entities[0]);
        canvasManipulator.showEntities(entities);
    }    
        
    function offVersionSelected(applicationEvent) {
        var entities = model.getEntitiesByVersion(applicationEvent.entities[0]);
        canvasManipulator.hideEntities(entities);
    }

    return {
        initialize: initialize,
		activate: activate
    };    
})();
