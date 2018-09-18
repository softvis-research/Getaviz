var configurationController = (function() {

    //config parameters
    let controllerConfig = {
        changeFrequency : false,
        antipattern: false,
        issues: false
    };

    let minChangeFrequency = 0;
    let issuesFilter = "showAll";

	function initialize(setupConfig){
        application.transferConfigParams(setupConfig, controllerConfig);
    }
	
	function activate(rootDiv){
        rootDiv.setAttribute("style","height:100%");
        if(controllerConfig.changeFrequency) {
            prepareChangeFrequency(rootDiv);
        }
        if(controllerConfig.antipattern) {
            prepareVisibilityLevelMenu(rootDiv);
            prepareBundledEdgesCheckbox(rootDiv);
        }
        if(controllerConfig.issues) {
            prepareIssuesFilter(rootDiv);
        }
    }

    function prepareIssuesFilter(rootDiv) {
	    const divElement = document.createElement("DIV");
	    const h = document.createElement("H4");
	    const textNode = document.createTextNode("Class Filter");
	    h.appendChild(textNode);
	    rootDiv.appendChild(h);
        rootDiv.appendChild(divElement);
        prepareIssuesRadioButton(divElement, "showAll", "Show all classes", true);
        prepareIssuesRadioButton(divElement, "showOpen", "Show classes with open issues", false);
        prepareIssuesRadioButton(divElement, "showOpenSecurity", "Show classes with open security issues", false);
    }

    function prepareIssuesRadioButton(rootDiv, filter, text, checked){
        const jqxId = "#" + filter;
        let divElement = document.createElement("DIV");
        const textNode = document.createTextNode(text);
        divElement.appendChild(textNode);
        divElement.id = filter;
        rootDiv.appendChild(divElement);
        $(jqxId).jqxRadioButton({ theme: "metro"});
        if(checked) {
            $(jqxId).jqxRadioButton('check');
        }
        $(jqxId).bind('checked', function () {
            issuesFilter = filter;
            //settingsChanged(issuesFilter, minChangeFrequency);
            const applicationEvent = {
                sender: configurationController,
                changeFrequency: minChangeFrequency,
                issuesFilter: issuesFilter
            };
            events.config.filterSettings.publish(applicationEvent);
        });
    }

    function prepareChangeFrequency(rootDiv){
        const id = "frequencySlider";
        const jqxId = "#" + id;
        let divElement = document.createElement("DIV");
        const h = document.createElement("H4");
        const textNode = document.createTextNode("Minimal change frequency of classes");
        h.appendChild(textNode);
        rootDiv.appendChild(h);
        divElement.id = id;
        rootDiv.appendChild(divElement);
        $(jqxId).jqxSlider({
            theme: "metro",
            showTicks: true,
            showTickLabels: true,
            tooltip: false,
            showButtons: false,
            min: 0,
            max: 1,
            height: 60,
            width: "100%",
            ticksPosition: 'bottom',
            showRange: true,
            tickLabelFormatFunction: function (value) {
                if (value == 0) return "min";
                if (value == 1) return "max";
                return "";
            }
        });
        $(jqxId).bind('slideEnd', function (event) {
            minChangeFrequency = event.args.value;
            const applicationEvent = {
                sender: configurationController,
                changeFrequency: minChangeFrequency,
                issuesFilter: issuesFilter
            };
            events.config.filterSettings.publish(applicationEvent);
        });
    }

    function prepareVisibilityLevelMenu(rootDiv){
        let a = document.createElement('a');
        const text = document.createTextNode("Visibility Level of Circular Dependencies");
        a.appendChild(text);
        a.href ="javascript: window.open(\"./glossary.html#level\",'glossary');";
        rootDiv.appendChild(a);
        prepareRadioButton(rootDiv, "showall", "Show all dependencies", 0);
        prepareRadioButton(rootDiv, "showcritical", "Only show critical dependencies", 0.25);
        prepareRadioButton(rootDiv, "showverycritical", "Only show very critical dependencies", 0.5);
        $('#showverycritical').jqxRadioButton('check');
    }

    function prepareRadioButton(rootDiv, id, text, value){
        const jqxId = "#" + id;
        let divElement = document.createElement("DIV");
        const textNode = document.createTextNode(text);
        divElement.appendChild(textNode);
        divElement.id = id;
        rootDiv.appendChild(divElement);
        $(jqxId).jqxRadioButton({ theme: "metro"});
        $(jqxId).bind('checked', function (event) {
            const applicationEvent = {
                sender: configurationController,
                entities: [value]
            };
            events.config.weight.publish(applicationEvent);
        });
    }

    function prepareBundledEdgesCheckbox(rootDiv) {
        const bundledEdgesCheckboxID = "#bundleCheckbox";
        let divElement = document.createElement("DIV");
        const textNode = document.createTextNode("Bundle dependencies");
        divElement.id = "bundleCheckbox";
        divElement.appendChild(textNode);
        rootDiv.appendChild(divElement);
        $(bundledEdgesCheckboxID).jqxCheckBox({theme: "metro", checked: false});
        $(bundledEdgesCheckboxID).on('checked', function () {
            const applicationEvent = {
                sender: configurationController,
                entities: [true]
            };
            events.config.bundledEdges.publish(applicationEvent);
        });
        $(bundledEdgesCheckboxID).on('unchecked', function () {
            const applicationEvent = {
                sender: configurationController,
                entities: [false]
            };
            events.config.bundledEdges.publish(applicationEvent);
        });
    }

    function settingsChanged(issueFilter, changeFrequency) {
        const entities = model.getEntitiesByType("Class");
        let hideEntities = [];
        let fadeEntities = [];
        if(issueFilter === "showAll") {
            entities.forEach(function(entity){
                if(entity.changeFrequency >= changeFrequency) {
                    if(entity.filtered) {
                        fadeEntities.push(entity);
                    }
                } else {
                    if(!entity.filtered) {
                        hideEntities.push(entity);
                    }
                }
            });
        } else {
            if(issueFilter === "showOpen") {
                entities.forEach(function(entity){
                    if(entity.changeFrequency >= changeFrequency) {
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
                            if(entity.filtered) {
                                fadeEntities.push(entity);
                            }
                        } else {
                            if(!entity.filtered) {
                                hideEntities.push(entity);
                            }
                        }
                    } else {
                        if(!entity.filtered) {
                            hideEntities.push(entity);
                        }
                    }
                });
            } else {
                entities.forEach(function (entity) {
                    if (entity.changeFrequency >= changeFrequency) {
                        let foundOpenIssues = false;
                        entity.issues.forEach(function (issueId) {
                            if (issueId !== "") {
                                const issue = model.getIssuesById(issueId);
                                if (issue.open && issue.security) {
                                    foundOpenIssues = true;
                                }
                            }
                        });
                        if (foundOpenIssues) {
                            if(entity.filtered) {
                                fadeEntities.push(entity);
                            }
                        } else {
                            if(!entity.filtered) {
                                hideEntities.push(entity);
                            }
                        }
                    } else {
                        if(!entity.filtered) {
                            hideEntities.push(entity);
                        }
                    }
                });
            }
        }
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
	
	function reset(){
	}
    
    return {
        initialize: initialize,
		activate: activate,
		reset: reset
    };
})();
