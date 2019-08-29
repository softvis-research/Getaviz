var metricAnimationController = (function() {

    let metricMaxValues = new Map();    // map: metricName - maxValue
    let activeAnimations = new Map();   // list of metricNames of the currently active animations

    var controllerConfig = {
        minBlinkingFrequency: 3000,          // milliseconds - min freq for blinking animation
        maxBlinkingFrequency: 500,           // milliseconds - max freq for blinking animation
        metricValueTransformation: "square"  // transform the metric value to better differentiate values at the bounds
                                             // - focus on low values: 'logarithmic', 'root'
                                             // - focus on high values: 'square'
    };

    function initialize(setupConfig){
        application.transferConfigParams(setupConfig, controllerConfig);
    }

    function activate(rootDiv){
        let containerMethods= document.createElement("div");
        containerMethods.setAttribute("class", "grid-container")
        let containerClasses= document.createElement("div");
        containerClasses.setAttribute("class", "grid-container");

        appendHeader(rootDiv,"Method Metrics:")
        appendMetricSelectionUiComponent(containerMethods, "CallsIn", "incoming method calls:")
        appendMetricSelectionUiComponent(containerMethods, "CallsOut","outgoing method calls:")
        rootDiv.appendChild(containerMethods);

        appendHeader(rootDiv,"Class Metrics:")
        appendMetricSelectionUiComponent(containerClasses, "NrAttributes","number of attributes:")
        rootDiv.appendChild(containerClasses);

        initializeMetricMaxValues();
    }

    function appendHeader(container, headerText) {
        let headerElement = document.createElement("h3");
        headerElement.textContent = headerText;
        container.appendChild(headerElement);
    }

    function appendMetricSelectionUiComponent(container, metric, metricLabel) {
        let leftCell = document.createElement("div");
        let rightCell = document.createElement("div");
        leftCell.setAttribute("class", "formular-grid-item-left");
        rightCell.setAttribute("class", "formular-grid-item-right");
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
                if (intensity > 0){
                    // if the entity has already a blinking animation, add the color, create a new one else
                    let blinkingAnimation = entity.metricAnimationBlinking;
                    if (blinkingAnimation == undefined){
                        blinkingAnimation = new MetricAnimationBlinking(controllerConfig.minBlinkingFrequency, controllerConfig.maxBlinkingFrequency);
                        blinkingAnimation.addMetric(metric, color, intensity);
                        entity.metricAnimationBlinking = blinkingAnimation;
                    } else {
                        canvasManipulator.stopBlinkingAnimationForEntity(entity);   // stop the existing blinking animation before starting a new one
                        blinkingAnimation.addMetric(metric, color, intensity);
                    }
                    canvasManipulator.startBlinkingAnimationForEntity(entity, blinkingAnimation);
                }
            });
        }
        else if (animation === "size"){
            entities.forEach(function (entity) {
                let intensity = getAnimationIntensity(entity, metric);
                canvasManipulator.startGrowShrinkAnimationForEntity(entity, intensity);
            });
        }
        else if (animation === "nothing"){
            entities.forEach(function (entity) {
                let blinkingAnimation = entity.metricAnimationBlinking;
                if (blinkingAnimation != undefined){
                    blinkingAnimation.removeMetric(metric);
                    if (blinkingAnimation.hasMetric()){
                        canvasManipulator.startBlinkingAnimationForEntity(entity, blinkingAnimation);
                    }
                }
            });
        }
    }

    function stopAnimationForMetric(entities, metric) {
        let runningAnimation = activeAnimations.get(metric);

        if (runningAnimation.startsWith("blinking_")){
            entities.forEach(function (entity) {
                canvasManipulator.stopBlinkingAnimationForEntity(entity);
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

        switch (controllerConfig.metricValueTransformation) {
            case "logarithmic":
                entitiesValue = Math.log2(1 + entitiesValue);
                maxValue = Math.log2(1 + maxValue);
                break;
            case "root":
                entitiesValue = Math.sqrt(entitiesValue);
                maxValue = Math.sqrt(maxValue);
                break;
            case "square":
                entitiesValue = entitiesValue * entitiesValue;
                maxValue = maxValue * maxValue;
                break;
        }

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

