var canvasFilterController = (function() {

    function initialize(){
    }
	
	function activate(){
		events.filtered.on.subscribe(onEntityFilter);
        events.tmpFiltered.on.subscribe(onEntityTmpFilter);
        events.filtered.off.subscribe(onEntityUnfilter);
        events.tmpFiltered.off.subscribe(onEntityTmpUnfilter);
        events.versionSelected.on.subscribe(onVersionSelected);
		events.versionSelected.off.subscribe(offVersionSelected);
        events.config.filterSettings.subscribe(filterSettings);
        events.macroChanged.on.subscribe(onMacroChanged);
    }

    function filterSettings(applicationEvent) {

        const entities = model.getEntitiesByType("Class");
        const fadeEntities = [];
        const hideEntities = [];
        entities.forEach(function(entity){
            hideEntities.push(entity);
        });

        if(hideEntities.length > 0) {
            const hideEvent = {
                sender: configurationController,
                entities: hideEntities
            };
            events.filtered.on.publish(hideEvent);
        }
        if(fadeEntities.length > 0) {
            const fadeEvent = {
                sender: configurationController,
                entities: fadeEntities
            };
            events.filtered.off.publish(fadeEvent);
        }

    }
		
	function onEntityFilter(applicationEvent) {				
		const entities = applicationEvent.entities;
		canvasManipulator.hideEntities(entities);
	}
	
	function onEntityUnfilter(applicationEvent) {
		const entities = applicationEvent.entities;
		canvasManipulator.showEntities(entities);
	}

    function onEntityTmpFilter(applicationEvent) {
        const entities = applicationEvent.entities;
        let stillTmpFiltered = [];
        entities.forEach(function(entity){
            if(!entity.filter){
                stillTmpFiltered.push(entity);
            }
        });
        canvasManipulator.hideEntities(stillTmpFiltered);

    }

    function onEntityTmpUnfilter(applicationEvent) {
        const entities = applicationEvent.entities;
        canvasManipulator.showEntities(entities);
    }
	
	function onVersionSelected(applicationEvent) {
        const entities = model.getEntitiesByVersion(applicationEvent.entities[0]);
        canvasManipulator.showEntities(entities);
    }    
        
    function offVersionSelected(applicationEvent) {
        const entities = model.getEntitiesByVersion(applicationEvent.entities[0]);
        canvasManipulator.hideEntities(entities);
    }

    function onMacroChanged(applicationEvent){
        const entities = model.getModelElementsByMacro(String(applicationEvent.entities[0]));
        var shownEntities = [];
        var hiddenEntities = [];
        for(var i = 0; i < entities.length; i++){
            if(showEntity(entities[i], applicationEvent)){
                shownEntities.push(entities[i]);
            } else {
                hiddenEntities.push(entities[i]);
            }
        }
        if(shownEntities.length > 0){
            //there are two modes: "transparent" and "removed"
            if(applicationEvent.filterMode === "transparent"){
                canvasManipulator.changeTransparencyOfEntities(shownEntities, 0.0);
            } else{
                canvasManipulator.showEntities(shownEntities);
            }
        }

        if(hiddenEntities.length > 0){
            if(applicationEvent.filterMode === "transparent"){
                canvasManipulator.changeTransparencyOfEntities(hiddenEntities, 0.85);
            } else {
                canvasManipulator.hideEntities(hiddenEntities);
            }
        }
    }

    function showEntity(modelEntity, applicationEvent){
        var showEntity = true;
        var condition = model.getEntityById(modelEntity.dependsOn);

        if(condition !== undefined){
            showEntity = evaluateCondition(condition, applicationEvent.allTreeNodesById);
        }
        
        return showEntity;
    }

    function evaluateCondition(condition, allTreeNodesById){
        switch (condition.type) {
            case "Macro":
                var macroNode = allTreeNodesById.get(condition.id);
                if(macroNode !== undefined && macroNode.checked){
                    return true;
                } else {
                    return false;
                }
            case "Negation":
                var negatedCondition = model.getEntityById(condition.negated);
                var macroResult = evaluateCondition(negatedCondition, allTreeNodesById);
                if(macroResult){
                    return false;
                //negation is true if macro is not defined
                } else {
                    return true;
                }
            case "And":
                var connectedEntityIds = condition.connected;
                var result = true;
                for(var i = 0; i < connectedEntityIds.length; i++){
                    var connectedEntity = model.getEntityById(connectedEntityIds[i]);
                    var singleResult = evaluateCondition(connectedEntity, allTreeNodesById);
                    //if one part of the and expression is false, the whole expression is false
                    if(singleResult === false){
                        result = false;
                        break;
                    }
                }
                return result;
            case "Or":
                var connectedEntityIds = condition.connected;
                var trueCounter = 0;
                for(var i = 0; i < connectedEntityIds.length; i++){
                    var connectedEntity = model.getEntityById(connectedEntityIds[i]);
                    var singleResult = evaluateCondition(connectedEntity, allTreeNodesById);
                    //an exclusive or is only true if exactly one of the parts is true
                    if(singleResult){
                        trueCounter += 1;
                        if(trueCounter > 1){
                            result = false;
                            break;
                        }
                    }
                }
                if(trueCounter === 0){
                    result = false;
                }
                return result;
            default:
                break;
        }
    }

    return {
        initialize: initialize,
		activate: activate
    };    
})();
