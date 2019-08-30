var metricAnimationController = (function() {

    let metricMaxValues = new Map();         // map: metricName - maxValue
    let activeAnimations = new Map();        // list of metricNames of the currently active animations
    let availableAnimations = new Map();     // all animations with description
    let availableAnimationsIncludingNothing = new Map();    // availableAnimations + nothing => used for combobox ui
    let availableMethodMetrics = new Map();  // all metrics for method entities + description
    let availableClassMetrics = new Map();   // all metrics for class entities + description
    let availableMetrics = new Map();        // availableMethodMetrics + availableClassMetrics

    var controllerConfig = {
        widgetUi: "list",                    // # UI Type:
                                             // - list
                                             // - combobox
        blinkingAnimationColors:             // # available colors for blinking animation
            [ "red", "green", "blue" ],
        minBlinkingFrequency: 3000,          // # milliseconds - min freq for blinking animation
        maxBlinkingFrequency: 500,           // # milliseconds - max freq for blinking animation
        metricValueTransformation: "square", // # transform the metric value to better differentiate values at the bounds
                                             // - focus on low values: 'logarithmic', 'root'
                                             // - focus on high values: 'square'
        expandAnimationType: "frequency",    // # type of the expanding animation
                                             // - frequency: grow and shrink to the double size - metricValue = frequency
                                             // - size: grow and shrink to a size depending on the metric value
        minExpandingFrequency: 3000,         // # milliseconds - min freq for expanding animation
                                             //   AND used as frequency for expandAnimationType: size
        maxExpandingFrequency: 100,          // # milliseconds - max freq for expanding animation
        defaultExpandScale: 2,               // expandAnimationType: frequency => expand always to this scale
        maxExpandingScale: 3                 // expandAnimationType: size => expands scale between 1 and maxScale
    };

    function initialize(setupConfig){
        application.transferConfigParams(setupConfig, controllerConfig);
        initializeAvailableMetrics();
    }

    function initializeAvailableMetrics() {
        for (let color of controllerConfig.blinkingAnimationColors){
            availableAnimations.set("blinking " + color, "blinking_" + color);
        }
        availableAnimations.set("change size", "size");

        availableAnimationsIncludingNothing.set("do nothing", "nothing");
        availableAnimations.forEach((value, key) => availableAnimationsIncludingNothing.set(key, value));

        availableMethodMetrics.set("incoming method calls", "CallsIn");
        availableMethodMetrics.set("outgoing method calls", "CallsOut");

        availableClassMetrics.set("number of attributes", "NrAttributes");

        availableMetrics = new Map([...availableMethodMetrics, ...availableClassMetrics]);
    }

    function activate(rootDiv){
        switch (controllerConfig.widgetUi) {
            case "list":
                activateListUi(rootDiv);
                break;
            case "combobox":
                activateComboboxUi(rootDiv);
                break;
            default:
                let wrongUiTypeText = document.createTextNode("Wrong widgetUi in setup. Possible values: list, combobox");
                rootDiv.append(wrongUiTypeText);
                break;
        }

        initializeMetricMaxValues();
    }


    /**
     * Adds the ui components to the widget for controllerConfig.widgetUi=list
     */
    function activateListUi(rootDiv) {
        let listSize = Math.max(availableMetrics.size, availableAnimations.size);

        let container = document.createElement("div");
        container.setAttribute("class", "grid-container metricAnimationGrid");

        let headerCellLeft = document.createElement("div");
        headerCellLeft.setAttribute("class", "grid-item");
        appendHeader(headerCellLeft,"Metrics:");
        container.appendChild(headerCellLeft);

        let headerCellRight = document.createElement("div");
        headerCellRight.setAttribute("class", "grid-item");
        appendHeader(headerCellRight,"Animations:");
        container.appendChild(headerCellRight);

        let listCellLeft = document.createElement("div");
        listCellLeft.setAttribute("class", "grid-item select-wrapper-level1");

        let metricSelectWrapper = document.createElement("div");
        metricSelectWrapper.setAttribute("class", "select-wrapper-level2");
        let metricsList = document.createElement("select");
        metricsList.setAttribute("size", listSize);
        appendOptionsToSelectElement(metricsList, availableMetrics);
        metricsList.onchange = function () {metricListSelectionChanged(metricsList, animationList); };
        metricSelectWrapper.appendChild(metricsList);
        listCellLeft.appendChild(metricSelectWrapper);
        container.appendChild(listCellLeft);

        let listCellRight = document.createElement("div");
        listCellRight.setAttribute("class", "grid-item select-wrapper-level1");

        let animationSelectWrapper = document.createElement("div");
        animationSelectWrapper.setAttribute("class", "select-wrapper-level2");
        let animationList = document.createElement("select");
        animationList.setAttribute("size", listSize);
        animationList.setAttribute("multiple", "multiple");
        animationList.setAttribute("disabled", "disabled");
        appendOptionsToSelectElement(animationList, availableAnimations);
        animationList.onchange = function () {animationListSelectionChanged(metricsList, animationList); };
        animationSelectWrapper.appendChild(animationList);
        listCellRight.appendChild(animationSelectWrapper);
        container.appendChild(listCellRight);

        rootDiv.appendChild(container);
    }

    /**
     * Adds the ui components to the widget for controllerConfig.widgetUi=combobox
     */
    function activateComboboxUi(rootDiv) {
        let containerMethods= document.createElement("div");
        containerMethods.setAttribute("class", "grid-container");
        let containerClasses= document.createElement("div");
        containerClasses.setAttribute("class", "grid-container");

        appendHeader(rootDiv,"Method Metrics:");
        for (let [desc, methodMetric] of availableMethodMetrics) {
            appendMetricSelectionUiComponent(containerMethods, methodMetric, desc);
        }
        rootDiv.appendChild(containerMethods);

        appendHeader(rootDiv,"Class Metrics:");
        for (let [desc, classMetric] of availableClassMetrics) {
            appendMetricSelectionUiComponent(containerClasses, classMetric, desc);
        }
        rootDiv.appendChild(containerClasses);
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
        appendOptionsToSelectElement(selectElement, availableAnimationsIncludingNothing);
        selectElement.onchange = function () {metricSelectionChanged(this, metric); };
        leftCell.appendChild(labelElement);
        rightCell.appendChild(selectElement);
        container.appendChild(leftCell);
        container.appendChild(rightCell);
    }

    function appendOptionsToSelectElement(selectElement, optionsMap) {
        for (const [key, value] of optionsMap.entries()) {
            let option = document.createElement("option");
            option.setAttribute("value", value);
            let optionLabel = document.createTextNode(key);
            option.appendChild(optionLabel);
            selectElement.appendChild(option);
        }
    }

    function metricListSelectionChanged(metricsList, animationList) {
        if (metricsList.value !== undefined){
            animationList.removeAttribute("disabled");
            restoreAnimationsList(metricsList.value, animationList);
        } else {
            animationList.setAttribute("disabled", "disabled");
        }
    }

    function animationListSelectionChanged(metricsList, animationList) {
        let selectedAnimations = [];
        for ( let i = 0; i < animationList.options.length; i++ )
        {
            let option = animationList.options[i];
            if (option.selected){
                selectedAnimations.push(option.value);
            }
        }
        startAnimationsForMetric(metricsList.value, selectedAnimations);
        activeAnimations.set(metricsList.value, selectedAnimations);
    }

    /**
     * Restores the selections of the animations list when another metric in the metric list was selected.
     */
    function restoreAnimationsList(metric, animationList) {
        let activeMetricAnimations = activeAnimations.get(metric);
        if (activeMetricAnimations === undefined){
            activeMetricAnimations = [];
        }
        for ( let i = 0; i < animationList.options.length; i++ )
        {
            let option = animationList.options[i];
            if (activeMetricAnimations.includes(option.value)){
                option.selected = true;
            } else {
                option.selected = false;
            }
        }
    }

    function stopAnimationsForMetric(entities, metric) {
        let runningAnimations = activeAnimations.get(metric);

        if (runningAnimations.includes("size")){
            entities.forEach(function (entity) {
                canvasManipulator.stopExpandingAnimationForEntity(entity);
            });
        }
        for (let i = 0; i < runningAnimations.length; i++){
            if (runningAnimations[i].startsWith("blinking_")){
                entities.forEach(function (entity) {
                    canvasManipulator.stopBlinkingAnimationForEntity(entity);
                });
                i = runningAnimations.length;
            }
        }

        activeAnimations.delete(metric);
    }

    function startAnimationsForMetric(metric, animations) {
        let entities;
        if (Array.from(availableMethodMetrics.values()).includes(metric)){
            entities = model.getEntitiesByType("Method");
        }
        else if (Array.from(availableClassMetrics.values()).includes(metric)){
            entities = model.getEntitiesByType("Class");
        }

        if (activeAnimations.has(metric)){
            // if there are already running animations for this metric: stop them!
            stopAnimationsForMetric(entities, metric);
        }

        let blinkingColors = [];
        animations.forEach(function (animation) {
            if (animation.startsWith("blinking_")){
                let color = animation.substring("blinking_".length);
                blinkingColors.push(color);
            }
            else if (animation === "size") {
                entities.forEach(function (entity) {
                    let intensity = getAnimationIntensity(entity, metric);
                    if (intensity > 0) {
                        // expandingAnimation get's not stored as entity attribute, because this is not necessary by now
                        let expandingAnimation = new MetricAnimationExpanding(controllerConfig.expandAnimationType,
                            controllerConfig.minExpandingFrequency, controllerConfig.maxExpandingFrequency,
                            controllerConfig.maxExpandingScale, controllerConfig.defaultExpandScale, intensity);

                        canvasManipulator.startExpandingAnimationForEntity(entity, expandingAnimation);
                    }
                });
            }
            else if (animation === "nothing"){
                removeBlinkingAnimationForMetricAndRestartRemaining(entities, metric);
            }
        });
        if (blinkingColors.length > 0){
            entities.forEach(function (entity) {
                let intensity = getAnimationIntensity(entity, metric);
                if (intensity > 0){
                    // if the entity has already a blinking animation, add the color, create a new one else
                    let blinkingAnimation = entity.metricAnimationBlinking;
                    if (blinkingAnimation === undefined){
                        blinkingAnimation = new MetricAnimationBlinking(controllerConfig.minBlinkingFrequency, controllerConfig.maxBlinkingFrequency);
                        blinkingAnimation.addMetric(metric, blinkingColors, intensity);
                        entity.metricAnimationBlinking = blinkingAnimation;
                    } else {
                        canvasManipulator.stopBlinkingAnimationForEntity(entity);   // stop the existing blinking animation before starting a new one
                        blinkingAnimation.addMetric(metric, blinkingColors, intensity);
                    }
                    canvasManipulator.startBlinkingAnimationForEntity(entity, blinkingAnimation);
                }
            });
        }
        if (animations.length === 0){
            removeBlinkingAnimationForMetricAndRestartRemaining(entities, metric);
        }
    }

    /**
     * Deletes the metric from the entities blinkingAnimations.
     * Restarts the remaining blinking animations for other metrics of the entity, if they where running.
     */
    function removeBlinkingAnimationForMetricAndRestartRemaining(entities, metric) {
        entities.forEach(function (entity) {
            let blinkingAnimation = entity.metricAnimationBlinking;
            if (blinkingAnimation !== undefined){
                blinkingAnimation.removeMetric(metric);
                if (blinkingAnimation.hasMetric()){
                    canvasManipulator.startBlinkingAnimationForEntity(entity, blinkingAnimation);
                }
            }
        });
    }

    function metricSelectionChanged(sel, metric) {
        let selectedAnimation = sel.value;

        startAnimationsForMetric(metric, [selectedAnimation]);
        activeAnimations.set(metric, [selectedAnimation]);
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

