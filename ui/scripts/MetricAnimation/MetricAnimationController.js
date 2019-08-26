var metricAnimationController = (function() {

    let metricMaxValues = new Map();
    let activeAnimations = new Map();

    var controllerConfig = {
        minBlinkingFrequency: 3000      // milliseconds - min freq for blinking animation
    };

    function initialize(setupConfig){
        application.transferConfigParams(setupConfig, controllerConfig);
    }

    function activate(rootDiv){
        let container= document.createElement("DIV");
        container.setAttribute("class", "grid-container");
        container.id = "metricAnimationDiv";

        appendMetricSelectionUiComponent(container, "CallsIn", "incoming method calls:")
        appendMetricSelectionUiComponent(container, "CallsOut","outgoing method calls:")
        appendMetricSelectionUiComponent(container, "NrAttributes","number of attributes:")

        rootDiv.appendChild(container);

        initializeMetricMaxValues();
    }

    function appendMetricSelectionUiComponent(container, metric, metricLabel) {
        let leftCell = document.createElement("div");
        let rightCell = document.createElement("div");
        // leftCell.setAttribute("class", "grid-item-left");
        // rightCell.setAttribute("class", "grid-item-right");
        let labelElement = document.createTextNode(metricLabel);
        let selectElement = document.createElement("select");
        selectElement.id = "select" + metric;
        let option0 = document.createElement("option");
        let option1 = document.createElement("option");
        let option2 = document.createElement("option");
        let option3 = document.createElement("option");
        let option4 = document.createElement("option");
        option0.setAttribute("value", "nothing");
        option1.setAttribute("value", "blinking_red");
        option2.setAttribute("value", "blinking_green");
        option3.setAttribute("value", "blinking_blue");
        option4.setAttribute("value", "size");
        let labelOption0 = document.createTextNode("do nothing");
        let labelOption1 = document.createTextNode("blinking red");
        let labelOption2 = document.createTextNode("blinking green");
        let labelOption3 = document.createTextNode("blinking blue");
        let labelOption4 = document.createTextNode("change size");
        option0.appendChild(labelOption0);
        option1.appendChild(labelOption1);
        option2.appendChild(labelOption2);
        option3.appendChild(labelOption3);
        option4.appendChild(labelOption4);
        selectElement.appendChild(option0);
        selectElement.appendChild(option1);
        selectElement.appendChild(option2);
        selectElement.appendChild(option3);
        selectElement.appendChild(option4);
        selectElement.onchange = function () {metricSelectionChanged(this); };
        leftCell.appendChild(labelElement);
        rightCell.appendChild(selectElement);
        container.appendChild(leftCell);
        container.appendChild(rightCell);
    }

    function metricSelectionChanged(sel) {
        let selectedMetric = sel.id.substring("select".length);
        let selectedVisualization = sel.value;

        const methods = model.getEntitiesByType("Method");
        const classes = model.getEntitiesByType("Class");

        switch(selectedMetric) {
            case "CallsIn":
                startAnimationForEntities(methods, selectedMetric, selectedVisualization);
                break;
            case "CallsOut":
                startAnimationForEntities(methods, selectedMetric, selectedVisualization);
                break;
            case "NrAttributes":
                startAnimationForEntities(classes, selectedMetric, selectedVisualization);
                break;
            default:
                events.log.error.publish({text: "MetricAnimationController - metricSelectionChanged - unknown metric: " + selectedMetric});
                return;
        }

        activeAnimations.set(selectedMetric, selectedVisualization);
    }


    function startAnimationForEntities(entities, metric, animation) {

        if (activeAnimations.has(metric)){
            stopAnimationForMetric(entities, metric);
        }

        if (animation.startsWith("blinking_")){
            let color = animation.substring("blinking_".length);

            entities.forEach(function (entity) {
                let intensity = getAnimationIntensity(entity, metric);
                canvasManipulator.startBlinkingAnimationForEntity(entity, color, intensity, controllerConfig.minBlinkingFrequency, metric);
            });
        }
        else if (animation === "size"){
            entities.forEach(function (entity) {
                let intensity = getAnimationIntensity(entity, metric);
                canvasManipulator.startGrowShrinkAnimationForEntity(entity, intensity);
            });
        }
    }


    function stopAnimationForMetric(entities, metric) {
        let runningAnimation = activeAnimations.get(metric);

        if (runningAnimation.startsWith("blinking_")){
            entities.forEach(function (entity) {
                canvasManipulator.stopBlinkingAnimationForEntity(entity, metric);
            });
        }
        else if (runningAnimation === "size"){
            entities.forEach(function (entity) {
                canvasManipulator.stopGrowShrinkAnimationForEntity(entity);
            });
        }

        activeAnimations.delete(metric);
    }

    function getAnimationIntensity(entity, metric) {

        let entitiesValue = getMetricValueOfEntity(entity, metric);
        let maxValue = getMetricMaxValue(metric);

        let intensity =  entitiesValue / maxValue;

        return intensity;
    }

    function getMetricMaxValue(metric){
        let maxValue = metricMaxValues.get(metric);
        if(maxValue === undefined) {
            events.log.error.publish({text: "MetricAnimationController - getMetricMaxValue - maxValue for metric not found"});
        }
        return maxValue;
    }

    function initializeMetricMaxValues() {
        const methods = model.getEntitiesByType("Method");
        const classes = model.getEntitiesByType("Class");

        metricMaxValues.set("CallsIn", getMetricMaxValueOfCollection(methods, "CallsIn"));
        metricMaxValues.set("CallsOut", getMetricMaxValueOfCollection(methods, "CallsOut"));
        metricMaxValues.set("NrAttributes", getMetricMaxValueOfCollection(classes, "NrAttributes"));
    }

    function getMetricMaxValueOfCollection(entities, metric) {
        let maxValue = -1;
        entities.forEach(function (entity) {
            let metricValue = getMetricValueOfEntity(entity, metric);
            if (metricValue > maxValue){
                maxValue = metricValue;
            }
        });
        return maxValue;
    }
    
    function getMetricValueOfEntity(entity, metric) {
        let result = -1;
        switch(metric) {
            case "CallsIn":
                result = entity.calls.length;
                break;
            case "CallsOut":
                result = entity.calledBy.length;
                break;
            case "NrAttributes":
                let nrAttributes = 0;
                entity.children.forEach(function (child) {
                    if (child.type === "Attribute"){
                        nrAttributes++;
                    }
                });
                result = nrAttributes;
                break;
            default:
                return;
        }
        return result;
    }

    function reset(){

    }

    function deactivate(){
        reset();
    }

    return {
        initialize: initialize,
        activate: activate,
        deactivate:	deactivate,
        reset: reset
    };
})();
