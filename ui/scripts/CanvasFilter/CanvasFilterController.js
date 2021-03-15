var canvasFilterController = (function() {

    let issueFilterId = "";
    let changeFrequency = 0;
    let issueFilter = "showAll";

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

    function applyIssueFilter(entities) {
        if(issueFilter === "showAll") {
            return entities;
        }
        if(issueFilter === "showOpen") {
            const result = [];
            entities.forEach(function(entity){
                let foundOpenIssues = false;
                entity.issues.forEach(function(issueId) {
                    if(issueId !== "") {
                        const issue = model.getIssuesById(issueId);
                        if (issue.open) {
                            foundOpenIssues = true;
                        }
                    }
                });
                if(foundOpenIssues) {
                    result.push(entity)
                }
            });
            return result;
        }
        if(issueFilter === "showOpenSecurity") {
            const result = [];
            entities.forEach(function(entity){
                let foundOpenIssues = false;
                entity.issues.forEach(function(issueId) {
                    if(issueId !== "") {
                        const issue = model.getIssuesById(issueId);
                        if (issue.open && issue.security) {
                            foundOpenIssues = true;
                        }
                    }
                });
                if(foundOpenIssues) {
                    result.push(entity)
                }
            });
            return result;
        }
    }

    function applyChangeFrequencyFilter(entities) {
        if (changeFrequency === 0) {
            return entities;
        } else {
            return entities.filter(entity => entity.changeFrequency >= changeFrequency);
        }
    }

    function applyIssueIdFilter(entities) {
        if(issueFilterId === "") {
            return entities;
        }
        return model.getEntitiesByIssue(issueFilterId);
    }

    function filterSettings(applicationEvent) {

        const entities = model.getEntitiesByType("Class");
        if (applicationEvent.changeFrequency !== undefined) {
            changeFrequency = applicationEvent.changeFrequency
        }
        if (applicationEvent.issuesFilter !== undefined) {
            issueFilter = applicationEvent.issuesFilter
        }
        if (applicationEvent.issueFilterId !== undefined) {
            issueFilterId = applicationEvent.issueFilterId;
        }
        let changeFrequencyEntities = applyChangeFrequencyFilter(entities);
        let issueFilterEntities = applyIssueFilter(entities);
        let issueIdEntities = applyIssueIdFilter(entities);
        const fadeEntities = [];
        const hideEntities = [];
        entities.forEach(function(entity){
            if(changeFrequencyEntities.includes(entity) && issueFilterEntities.includes(entity) && issueIdEntities.includes(entity)){
                fadeEntities.push(entity);
            } else {
                hideEntities.push(entity);
            }
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

        const selectedEntities = entities.filter(entity => entity.selected);
        if (selectedEntities.length) {
            const unselectEvent = {
                sender: canvasFilterController,
                entities: selectedEntities
            }
            events.selected.off.publish(unselectEvent);
        }

        canvasManipulator.hideEntities(entities, { name: "canvasFilterController" });
	}

	function onEntityUnfilter(applicationEvent) {
        const entities = applicationEvent.entities;
        canvasManipulator.showEntities(entities, { name: "canvasFilterController" });
	}

    function onEntityTmpFilter(applicationEvent) {
        const entities = applicationEvent.entities;
        let stillTmpFiltered = [];
        entities.forEach(function(entity){
            if(!entity.filter){
                stillTmpFiltered.push(entity);
            }
        });
        canvasManipulator.hideEntities(stillTmpFiltered, { name: "canvasFilterController" });
    }

    function onEntityTmpUnfilter(applicationEvent) {
        const entities = applicationEvent.entities;
        canvasManipulator.showEntities(entities, { name: "canvasFilterController" });
    }

	function onVersionSelected(applicationEvent) {
        const entities = model.getEntitiesByVersion(applicationEvent.entities[0]);
        canvasManipulator.showEntities(entities, { name: "canvasFilterController" });
    }

    function offVersionSelected(applicationEvent) {
        const entities = model.getEntitiesByVersion(applicationEvent.entities[0]);
        canvasManipulator.hideEntities(entities, { name: "canvasFilterController" });
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
                canvasManipulator.changeTransparencyOfEntities(shownEntities, 0.0, { name: "canvasFilterController" });
            } else{
                canvasManipulator.showEntities(shownEntities, { name: "canvasFilterController" });
            }
        }

        if(hiddenEntities.length > 0){
            if(applicationEvent.filterMode === "transparent"){
                canvasManipulator.changeTransparencyOfEntities(hiddenEntities, 0.85, { name: "canvasFilterController" });
            } else {
                canvasManipulator.hideEntities(hiddenEntities, { name: "canvasFilterController" });
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
