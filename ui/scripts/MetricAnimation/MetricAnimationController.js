var metricAnimationController = (function() {

    var controllerConfig = {
        bla : true
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
        option1.setAttribute("value", "red");
        option2.setAttribute("value", "green");
        option3.setAttribute("value", "blue");
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
        let currentMetric = sel.id;
        let currentVisualization = sel.value;

        console.log(currentMetric + " - " + currentVisualization);

        const entities = model.getEntitiesByType("Class");

        //canvasManipulator.changeColorOfEntities(entities, "green");

        canvasManipulator.startBlinkingAnimationForEntities(entities, "green");

        // Metric scheint man im model zu finden!
        // entity.calls
        // entity.calledBy
        // entity.accessedBy

        // entities.forEach(function(entity){
        //
        // });
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
