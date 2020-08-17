class DomHelper {

    constructor() {
        this.colors = [
            "red",
            "blue",
            "green",
            "black",
            "yellow",
            "orange"
        ];

    }

    buildUI(rootDiv, controllerConfig) {
        let cssLink = document.createElement("link");
        cssLink.type = "text/css";
        cssLink.rel = "stylesheet";
        cssLink.href = "scripts/Metric/metricBox.css";
        document.getElementsByTagName("head")[0].appendChild(cssLink);


        var executeButtonDiv = document.createElement("div");
        executeButtonDiv.id = domIDs.executeButton;
        executeButtonDiv.textContent = "Execute";
        rootDiv.appendChild(executeButtonDiv);

        $(cssIDs.executeButton).jqxButton({ theme: "metro", height: 20, width: "32%" });

        var resetButtonDiv = document.createElement("div");
        resetButtonDiv.id = domIDs.resetButton;
        resetButtonDiv.textContent = "Reset";
        rootDiv.appendChild(resetButtonDiv);

        $(cssIDs.resetButton).jqxButton({ theme: "metro", height: 20, width: "32%" });

        var addLayerButtonDiv = document.createElement("div");
        addLayerButtonDiv.id = domIDs.addLayerButton;
        addLayerButtonDiv.textContent = "Add Metric-Mapping";
        rootDiv.appendChild(addLayerButtonDiv);

        $(cssIDs.addLayerButton).jqxButton({ theme: "metro", height: 20, width: "32%" });

        // this.buildMetricArea(rootDiv, controllerConfig);

        // this.buildMappingArea(rootDiv, controllerConfig);

    }

    buildMetricArea(rootDiv, controllerConfig) {
        var metricTextNode = document.createElement("p");
        metricTextNode.id = "metrics";
        metricTextNode.classList.add("metrics");
        metricTextNode.textContent = "Metrics";
        rootDiv.appendChild(metricTextNode);

        var metricDropDownDiv = document.createElement("div");
        metricDropDownDiv.id = domIDs.metricDropDown;
        rootDiv.appendChild(metricDropDownDiv);

        $(cssIDs.metricDropDown).jqxDropDownList({ source: controllerConfig.metrics, placeHolder: "Select Metric", width: 250, height: 30, dropDownVerticalAlignment: "top" });

        $(cssIDs.metricDropDown).on("change", this.metricDropDownSelected);

        var metricFromTextNode = document.createElement("p");
        metricFromTextNode.id = domIDs.metricFromText;
        metricFromTextNode.classList.add("metricParameter");
        metricFromTextNode.textContent = "Metric - From";
        rootDiv.appendChild(metricFromTextNode);

        var metricFromInput = document.createElement("input");
        metricFromInput.type = "number";
        metricFromInput.id = domIDs.metricFromInput;
        metricFromInput.classList.add("metricParameter");
        rootDiv.appendChild(metricFromInput);

        $(cssIDs.metricFromInput).jqxInput({ placeHolder: "From", width: 100, height: 30, minLength: 1 });

        var metricFromDateInput = document.createElement("div");
        metricFromDateInput.id = domIDs.metricFromDateInput;
        metricFromDateInput.classList.add("metricParameter");
        rootDiv.appendChild(metricFromDateInput);

        $(cssIDs.metricFromDateInput).jqxDateTimeInput({ placeHolder: "YYYYMMDD", formatString: "yyyyMMdd", value: null, dropDownVerticalAlignment: "top", width: 100, height: 30 });


        var metricToTextNode = document.createElement("p");
        metricToTextNode.id = domIDs.metricToText;
        metricToTextNode.classList.add("metricParameter");
        metricToTextNode.textContent = "Metric - To";
        rootDiv.appendChild(metricToTextNode);

        var metricToInput = document.createElement("input");
        metricToInput.type = "number";
        metricToInput.id = domIDs.metricToInput;
        metricToInput.classList.add("metricParameter");
        rootDiv.appendChild(metricToInput);

        $(cssIDs.metricToInput).jqxInput({ placeHolder: "To", width: 100, height: 30, minLength: 1 });

        var metricToDateInput = document.createElement("div");
        metricToDateInput.id = domIDs.metricToDateInput;
        metricToDateInput.classList.add("metricParameter");
        rootDiv.appendChild(metricToDateInput);

        $(cssIDs.metricToDateInput).jqxDateTimeInput({ placeHolder: "YYYYMMDD", formatString: "yyyyMMdd", value: null, dropDownVerticalAlignment: "top", width: 100, height: 30 });

    }

    metricDropDownSelected() {
        $(cssIDs.metricFromText).show();
        $(cssIDs.metricToText).show();

        switch ($(cssIDs.metricDropDown).val()) {
            case metrics.numberOfStatements:
                $(cssIDs.metricFromDateInput).hide();
                $(cssIDs.metricToDateInput).hide();
                $(cssIDs.metricFromInput).show();
                $(cssIDs.metricToInput).show();
                break;
            case metrics.dateOfCreation:
            case metrics.dateOfLastChange:
                $(cssIDs.metricFromInput).hide();
                $(cssIDs.metricToInput).hide();
                $(cssIDs.metricFromDateInput).show();
                $(cssIDs.metricToDateInput).show();
                break;

        }
    }

    buildMappingArea(rootDiv, controllerConfig) {
        var mappingTextNode = document.createElement("p");
        mappingTextNode.id = "mappings";
        mappingTextNode.classList.add("mappings");
        mappingTextNode.textContent = "Mappings";
        rootDiv.appendChild(mappingTextNode);

        var mappingDropDownDiv = document.createElement("div");
        mappingDropDownDiv.id = domIDs.mappingDropDown;
        rootDiv.appendChild(mappingDropDownDiv);

        $(cssIDs.mappingDropDown).jqxDropDownList({ source: controllerConfig.mappings, placeHolder: "Select Mapping", width: 250, height: 30, dropDownVerticalAlignment: "top" });

        $(cssIDs.mappingDropDown).on("change", this.mappingDropDownSelected);


        var mappingFromTextNode = document.createElement("p");
        mappingFromTextNode.id = "mappingFromText";
        mappingFromTextNode.classList.add("mappingParameter");
        mappingFromTextNode.textContent = "Mapping - From";
        rootDiv.appendChild(mappingFromTextNode);

        var mappingFromInput = document.createElement("input");
        mappingFromInput.type = "text";
        mappingFromInput.id = "mappingFromInput";
        mappingFromInput.classList.add("mappingParameter");
        rootDiv.appendChild(mappingFromInput);

        $("#mappingFromInput").jqxInput({ placeHolder: "From", width: 100, height: 30, minLength: 1 });


        var mappingToTextNode = document.createElement("p");
        mappingToTextNode.id = "mappingToText";
        mappingToTextNode.classList.add("mappingParameter");
        mappingToTextNode.textContent = "Mapping - To";
        rootDiv.appendChild(mappingToTextNode);

        var mappingToInput = document.createElement("input");
        mappingToInput.type = "text";
        mappingToInput.id = "mappingToInput";
        mappingToInput.classList.add("mappingParameter");
        rootDiv.appendChild(mappingToInput);

        $("#mappingToInput").jqxInput({ placeHolder: "To", width: 100, height: 30, minLength: 1 });


        var mappingColorDropDownDiv = document.createElement("div");
        mappingColorDropDownDiv.id = domIDs.mappingColorDropDown;
        mappingColorDropDownDiv.classList.add("mappingParameter");
        rootDiv.appendChild(mappingColorDropDownDiv);

        $(cssIDs.mappingColorDropDown).jqxDropDownList({ source: this.colors, placeHolder: "Select Color", width: 250, height: 30, dropDownVerticalAlignment: "top" });


        var mappingStartColorDropDownDiv = document.createElement("div");
        mappingStartColorDropDownDiv.id = domIDs.mappingStartColorDropDown;
        mappingStartColorDropDownDiv.classList.add("mappingParameter");
        rootDiv.appendChild(mappingStartColorDropDownDiv);

        $(cssIDs.mappingStartColorDropDown).jqxDropDownList({ source: this.colors, placeHolder: "Select Start Color", width: 250, height: 30, dropDownVerticalAlignment: "top" });

        var mappingEndColorDropDownDiv = document.createElement("div");
        mappingEndColorDropDownDiv.id = domIDs.mappingEndColorDropDown;
        mappingEndColorDropDownDiv.classList.add("mappingParameter");
        rootDiv.appendChild(mappingEndColorDropDownDiv);

        $(cssIDs.mappingEndColorDropDown).jqxDropDownList({ source: this.colors, placeHolder: "Select End Color", width: 250, height: 30, dropDownVerticalAlignment: "top" });

        
        var transparencyInputDiv = document.createElement("div");
        transparencyInputDiv.id = domIDs.mappingTransparencyInput;
        transparencyInputDiv.classList.add("mappingParameter");
        rootDiv.appendChild(transparencyInputDiv);

        $(cssIDs.mappingTransparencyInput).jqxNumberInput({ width: 250, height: 30, min: 0, max: 1, inputMode: "simple", spinButtons: true });

        var mappingPeriodTextNode = document.createElement("p");
        mappingPeriodTextNode.id = domIDs.mappingPeriodText;
        mappingPeriodTextNode.classList.add("mappingParameter");
        mappingPeriodTextNode.textContent = "Period in ms";
        rootDiv.appendChild(mappingPeriodTextNode);

        var mappingPeriodInput = document.createElement("input");
        mappingPeriodInput.type = "number";
        mappingPeriodInput.id = domIDs.mappingPeriodInput;
        mappingPeriodInput.classList.add("mappingParameter");
        rootDiv.appendChild(mappingPeriodInput);

        $(cssIDs.mappingPeriodInput).jqxInput({ width: 100, height: 30, minLength: 1 });


        var mappingScaleTextNode = document.createElement("p");
        mappingScaleTextNode.id = domIDs.mappingScaleText;
        mappingScaleTextNode.classList.add("mappingParameter");
        mappingScaleTextNode.textContent = "Scale";
        rootDiv.appendChild(mappingScaleTextNode);

        var mappingScaleInput = document.createElement("input");
        mappingScaleInput.type = "number";
        mappingScaleInput.id = domIDs.mappingScaleInput;
        mappingScaleInput.classList.add("mappingParameter");
        rootDiv.appendChild(mappingScaleInput);

        $(cssIDs.mappingScaleInput).jqxInput({ width: 100, height: 30, minLength: 1 });
    }

    mappingDropDownSelected() {
        $(".mappingParameter").hide();
        switch ($(cssIDs.mappingDropDown).val()) {
            case mappings.color:
                $(cssIDs.mappingColorDropDown).show();
                break;
            case mappings.colorGradient:
                $(cssIDs.mappingStartColorDropDown).show();
                $(cssIDs.mappingEndColorDropDown).show();
                break;
            case mappings.transparency:
                $(cssIDs.mappingTransparencyInput).show();
                break;
            case mappings.pulsation:
                $(cssIDs.mappingPeriodText).show();
                $(cssIDs.mappingPeriodInput).show();
                $(cssIDs.mappingScaleText).show();
                $(cssIDs.mappingScaleInput).show();
                break;
            case mappings.flashing:
                $(cssIDs.mappingPeriodText).show();
                $(cssIDs.mappingPeriodInput).show();
                $(cssIDs.mappingColorDropDown).show();
                break;
            case mappings.rotation:
                $(cssIDs.mappingPeriodText).show();
                $(cssIDs.mappingPeriodInput).show();
                break;
        }
    }

    resetUI() {
        $(cssIDs.metricDropDown).jqxDropDownList("clearSelection");
        $(cssIDs.mappingDropDown).jqxDropDownList("clearSelection");

        $(".metricParameter").jqxInput("clear");
        $(".metricParameter").jqxDateTimeInput("clear");

        $(".mappingParameter").jqxInput("clear");
        $(".mappingParameter").jqxDropDownList("clearSelection");

        $(cssIDs.mappingColorDropDown).jqxDropDownList("clearSelection");

        $(".metricParameter").hide();
        $(".mappingParameter").hide();
    }

}