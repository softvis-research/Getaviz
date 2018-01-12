var canvasFilterController = (function() {
    
    function initialize(){ 
		
    }
	
	function activate(){
		events.filtered.on.subscribe(onEntityFilter);
		events.filtered.off.subscribe(onEntityUnfilter);  
	}
		
	function onEntityFilter(applicationEvent) {				
		var entities = applicationEvent.entities;
		canvasManipulator.hideEntities(entities);

	}
	
	function onEntityUnfilter(applicationEvent) {
		var entities = applicationEvent.entities;
		canvasManipulator.showEntities(entities);
	}
	

    return {
        initialize: initialize,
		activate: activate
    };    
})();