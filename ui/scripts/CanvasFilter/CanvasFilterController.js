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
        events.macroDefined.on.subscribe(onMacroDefined);
        events.macroDefined.off.subscribe(onMacroUndefined);
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

    function onMacroDefined(applicationEvent){
        const entities = model.getModelElementsByMacro(String(applicationEvent.entities[0]));
        canvasManipulator.showEntities(entities);
    }

    function onMacroUndefined(applicationEvent){
        const entities = model.getModelElementsByMacro(String(applicationEvent.entities[0]));
        canvasManipulator.hideEntities(entities);
    }

    return {
        initialize: initialize,
		activate: activate
    };    
})();
