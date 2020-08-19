class DomHelper {

    constructor(rootDiv, controllerConfig) {
        this.rootDiv = rootDiv;
        this.controllerConfig = controllerConfig;
        this.colors = [
            "red",
            "blue",
            "green",
            "black",
            "yellow",
            "orange"
        ];

    }

    buildUiHead() {
        let cssLink = document.createElement("link");
        cssLink.type = "text/css";
        cssLink.rel = "stylesheet";
        cssLink.href = "scripts/Metric/metricBox.css";
        document.getElementsByTagName("head")[0].appendChild(cssLink);


        var executeButtonDiv = document.createElement("div");
        executeButtonDiv.id = domIDs.executeButton;
        executeButtonDiv.textContent = "Execute";
        this.rootDiv.appendChild(executeButtonDiv);

        $(cssIDs.executeButton).jqxButton({ theme: "metro", height: 20, width: "32%" });

        var resetButtonDiv = document.createElement("div");
        resetButtonDiv.id = domIDs.resetButton;
        resetButtonDiv.textContent = "Reset";
        this.rootDiv.appendChild(resetButtonDiv);

        $(cssIDs.resetButton).jqxButton({ theme: "metro", height: 20, width: "32%" });

        var addLayerButtonDiv = document.createElement("div");
        addLayerButtonDiv.id = domIDs.addLayerButton;
        addLayerButtonDiv.textContent = "Add Metric-Layer";
        this.rootDiv.appendChild(addLayerButtonDiv);

        $(cssIDs.addLayerButton).jqxButton({ theme: "metro", height: 20, width: "32%" });

    }

    buildUiLayer(layerID) {
        this.buildMetricArea(layerID);
        this.buildMappingArea(layerID);
        this.buildDeleteButton(layerID);
    }

    buildMetricArea(layerID) {
        var metricTextNode = document.createElement("p");
        metricTextNode.id = "metrics" + layerID;
        metricTextNode.classList.add("metrics", "layer" + layerID);
        metricTextNode.textContent = "Metrics";
        this.rootDiv.appendChild(metricTextNode);

        var metricDropDownDiv = document.createElement("div");
        metricDropDownDiv.id = domIDs.metricDropDown + layerID;
        metricDropDownDiv.classList.add(domIDs.metricDropDown, "layer" + layerID);
        this.rootDiv.appendChild(metricDropDownDiv);

        $(cssIDs.metricDropDown + layerID).jqxDropDownList({
            source: metricController.controllerConfig.metrics,
            placeHolder: "Select Metric",
            width: 250, height: 30,
            dropDownVerticalAlignment: "top"
        });

        $(cssIDs.metricDropDown + layerID).on("change", () => { this.metricDropDownSelected(layerID) });

        var metricFromTextNode = document.createElement("p");
        metricFromTextNode.id = domIDs.metricFromText + layerID;
        metricFromTextNode.classList.add("metricParameter", "metricParameter" + layerID, "layer" + layerID);
        metricFromTextNode.textContent = "Metric - From";
        this.rootDiv.appendChild(metricFromTextNode);

        var metricFromInput = document.createElement("input");
        metricFromInput.type = "number";
        metricFromInput.id = domIDs.metricFromInput + layerID;
        metricFromInput.classList.add("metricParameter", "metricParameter" + layerID, "layer" + layerID);
        this.rootDiv.appendChild(metricFromInput);

        $(cssIDs.metricFromInput + layerID).jqxInput({ placeHolder: "From", width: 100, height: 30, minLength: 1 });

        var metricFromDateInput = document.createElement("div");
        metricFromDateInput.id = domIDs.metricFromDateInput + layerID;
        metricFromDateInput.classList.add("metricParameter", "metricParameter" + layerID, "layer" + layerID);
        this.rootDiv.appendChild(metricFromDateInput);

        $(cssIDs.metricFromDateInput + layerID).jqxDateTimeInput({
            placeHolder: "YYYYMMDD",
            formatString: "yyyyMMdd",
            value: null,
            dropDownVerticalAlignment: "top",
            width: 100, height: 30
        });


        var metricToTextNode = document.createElement("p");
        metricToTextNode.id = domIDs.metricToText + layerID;
        metricToTextNode.classList.add("metricParameter", "metricParameter" + layerID, "layer" + layerID);
        metricToTextNode.textContent = "Metric - To";
        this.rootDiv.appendChild(metricToTextNode);

        var metricToInput = document.createElement("input");
        metricToInput.type = "number";
        metricToInput.id = domIDs.metricToInput + layerID;
        metricToInput.classList.add("metricParameter", "metricParameter" + layerID, "layer" + layerID);
        this.rootDiv.appendChild(metricToInput);

        $(cssIDs.metricToInput + layerID).jqxInput({ placeHolder: "To", width: 100, height: 30, minLength: 1 });

        var metricToDateInput = document.createElement("div");
        metricToDateInput.id = domIDs.metricToDateInput + layerID;
        metricToDateInput.classList.add("metricParameter", "metricParameter" + layerID, "layer" + layerID);
        this.rootDiv.appendChild(metricToDateInput);

        $(cssIDs.metricToDateInput + layerID).jqxDateTimeInput({
            placeHolder: "YYYYMMDD",
            formatString: "yyyyMMdd",
            value: null,
            dropDownVerticalAlignment: "top",
            width: 100, height: 30
        });
    }

    metricDropDownSelected(layerID) {
        $(cssIDs.metricFromText + layerID).show();
        $(cssIDs.metricToText + layerID).show();

        switch ($(cssIDs.metricDropDown + layerID).val()) {
            case metrics.numberOfStatements:
                $(cssIDs.metricFromDateInput + layerID).hide();
                $(cssIDs.metricToDateInput + layerID).hide();
                $(cssIDs.metricFromInput + layerID).show();
                $(cssIDs.metricToInput + layerID).show();
                break;
            case metrics.dateOfCreation:
            case metrics.dateOfLastChange:
                $(cssIDs.metricFromInput + layerID).hide();
                $(cssIDs.metricToInput + layerID).hide();
                $(cssIDs.metricFromDateInput + layerID).show();
                $(cssIDs.metricToDateInput + layerID).show();
                break;
        }
    }

    buildMappingArea(layerID) {
        var mappingTextNode = document.createElement("p");
        mappingTextNode.id = "mappings" + layerID;
        mappingTextNode.classList.add("mappings", "layer" + layerID);
        mappingTextNode.textContent = "Mappings";
        this.rootDiv.appendChild(mappingTextNode);

        var mappingDropDownDiv = document.createElement("div");
        mappingDropDownDiv.id = domIDs.mappingDropDown + layerID;
        mappingDropDownDiv.classList.add(domIDs.mappingDropDown, "layer" + layerID);
        this.rootDiv.appendChild(mappingDropDownDiv);

        $(cssIDs.mappingDropDown + layerID).jqxDropDownList({
            source: metricController.controllerConfig.mappings,
            placeHolder: "Select Mapping",
            width: 250, height: 30,
            dropDownVerticalAlignment: "top"
        });

        $(cssIDs.mappingDropDown + layerID).on("change", () => { this.mappingDropDownSelected(layerID) });


        var mappingFromTextNode = document.createElement("p");
        mappingFromTextNode.id = "mappingFromText" + layerID;
        mappingFromTextNode.classList.add("mappingParameter", "mappingParameter" + layerID, "layer" + layerID);
        mappingFromTextNode.textContent = "Mapping - From";
        this.rootDiv.appendChild(mappingFromTextNode);

        var mappingFromInput = document.createElement("input");
        mappingFromInput.type = "text";
        mappingFromInput.id = "mappingFromInput" + layerID;
        mappingFromInput.classList.add("mappingParameter", "mappingParameter" + layerID, "layer" + layerID);
        this.rootDiv.appendChild(mappingFromInput);

        $("#mappingFromInput" + layerID).jqxInput({ placeHolder: "From", width: 100, height: 30, minLength: 1 });


        var mappingToTextNode = document.createElement("p");
        mappingToTextNode.id = "mappingToText" + layerID;
        mappingToTextNode.classList.add("mappingParameter", "mappingParameter" + layerID, "layer" + layerID);
        mappingToTextNode.textContent = "Mapping - To";
        this.rootDiv.appendChild(mappingToTextNode);

        var mappingToInput = document.createElement("input");
        mappingToInput.type = "text";
        mappingToInput.id = "mappingToInput" + layerID;
        mappingToInput.classList.add("mappingParameter", "mappingParameter" + layerID, "layer" + layerID);
        this.rootDiv.appendChild(mappingToInput);

        $("#mappingToInput" + layerID).jqxInput({ placeHolder: "To", width: 100, height: 30, minLength: 1 });


        var mappingColorDropDownDiv = document.createElement("div");
        mappingColorDropDownDiv.id = domIDs.mappingColorDropDown + layerID;
        mappingColorDropDownDiv.classList.add("mappingParameter", "mappingParameter" + layerID, "layer" + layerID);
        this.rootDiv.appendChild(mappingColorDropDownDiv);

        $(cssIDs.mappingColorDropDown + layerID).jqxDropDownList({
            source: colors,
            placeHolder: "Select Color",
            width: 250, height: 30,
            dropDownVerticalAlignment: "top"
        });


        var mappingStartColorDropDownDiv = document.createElement("div");
        mappingStartColorDropDownDiv.id = domIDs.mappingStartColorDropDown + layerID;
        mappingStartColorDropDownDiv.classList.add("mappingParameter", "mappingParameter" + layerID, "layer" + layerID);
        this.rootDiv.appendChild(mappingStartColorDropDownDiv);

        $(cssIDs.mappingStartColorDropDown + layerID).jqxDropDownList({
            source: colors,
            placeHolder: "Select Start Color",
            width: 250, height: 30,
            dropDownVerticalAlignment: "top"
        });

        var mappingEndColorDropDownDiv = document.createElement("div");
        mappingEndColorDropDownDiv.id = domIDs.mappingEndColorDropDown + layerID;
        mappingEndColorDropDownDiv.classList.add("mappingParameter", "mappingParameter" + layerID, "layer" + layerID);
        this.rootDiv.appendChild(mappingEndColorDropDownDiv);

        $(cssIDs.mappingEndColorDropDown + layerID).jqxDropDownList({
            source: colors,
            placeHolder: "Select End Color",
            width: 250, height: 30,
            dropDownVerticalAlignment: "top"
        });


        var transparencyInputDiv = document.createElement("div");
        transparencyInputDiv.id = domIDs.mappingTransparencyInput + layerID;
        transparencyInputDiv.classList.add("mappingParameter", "mappingParameter" + layerID, "layer" + layerID);
        this.rootDiv.appendChild(transparencyInputDiv);

        $(cssIDs.mappingTransparencyInput + layerID).jqxNumberInput({
            width: 250, height: 30,
            min: 0, max: 1,
            inputMode: "simple",
            spinButtons: true
        });

        var mappingPeriodTextNode = document.createElement("p");
        mappingPeriodTextNode.id = domIDs.mappingPeriodText + layerID;
        mappingPeriodTextNode.classList.add("mappingParameter", "mappingParameter" + layerID, "layer" + layerID);
        mappingPeriodTextNode.textContent = "Period in ms";
        this.rootDiv.appendChild(mappingPeriodTextNode);

        var mappingPeriodInput = document.createElement("input");
        mappingPeriodInput.type = "number";
        mappingPeriodInput.id = domIDs.mappingPeriodInput + layerID;
        mappingPeriodInput.classList.add("mappingParameter", "mappingParameter" + layerID, "layer" + layerID);
        this.rootDiv.appendChild(mappingPeriodInput);

        $(cssIDs.mappingPeriodInput + layerID).jqxInput({ width: 100, height: 30, minLength: 1 });


        var mappingScaleTextNode = document.createElement("p");
        mappingScaleTextNode.id = domIDs.mappingScaleText + layerID;
        mappingScaleTextNode.classList.add("mappingParameter", "mappingParameter" + layerID, "layer" + layerID);
        mappingScaleTextNode.textContent = "Scale";
        this.rootDiv.appendChild(mappingScaleTextNode);

        var mappingScaleInput = document.createElement("input");
        mappingScaleInput.type = "number";
        mappingScaleInput.id = domIDs.mappingScaleInput + layerID;
        mappingScaleInput.classList.add("mappingParameter", "mappingParameter" + layerID, "layer" + layerID);
        this.rootDiv.appendChild(mappingScaleInput);

        $(cssIDs.mappingScaleInput + layerID).jqxInput({ width: 100, height: 30, minLength: 1 });
    }

    mappingDropDownSelected(layerID) {
        $(".mappingParameter" + layerID).hide();

        switch ($(cssIDs.mappingDropDown + layerID).val()) {
            case mappings.color:
                $(cssIDs.mappingColorDropDown + layerID).show();
                break;
            case mappings.colorGradient:
                $(cssIDs.mappingStartColorDropDown + layerID).show();
                $(cssIDs.mappingEndColorDropDown + layerID).show();
                break;
            case mappings.transparency:
                $(cssIDs.mappingTransparencyInput + layerID).show();
                break;
            case mappings.pulsation:
                $(cssIDs.mappingPeriodText + layerID).show();
                $(cssIDs.mappingPeriodInput + layerID).show();
                $(cssIDs.mappingScaleText + layerID).show();
                $(cssIDs.mappingScaleInput + layerID).show();
                break;
            case mappings.flashing:
                $(cssIDs.mappingPeriodText + layerID).show();
                $(cssIDs.mappingPeriodInput + layerID).show();
                $(cssIDs.mappingColorDropDown + layerID).show();
                break;
            case mappings.rotation:
                $(cssIDs.mappingPeriodText + layerID).show();
                $(cssIDs.mappingPeriodInput + layerID).show();
                break;
        }
    }

    buildDeleteButton(layerID) {
        var deleteButtonDiv = document.createElement("div");
        deleteButtonDiv.id = domIDs.deleteButton + layerID;
        deleteButtonDiv.classList.add(domIDs.deleteButton, "layer" + layerID);
        this.rootDiv.appendChild(deleteButtonDiv);

        $(cssIDs.deleteButton + layerID).jqxButton({ theme: "metro", height: 30, width: 30, imgSrc: "./scripts/Metric/close.png" });
        
        $(cssIDs.deleteButton + layerID).click((event) => metricController.removeLayer(event));
    }

    destroyLayerUI(layerID) {
        this.resetLayerUI(layerID);
        $(".layer" + layerID).remove();        
    }

    resetLayerUI(layerID) {
        $(cssIDs.metricDropDown + layerID).jqxDropDownList("clearSelection");
        $(cssIDs.mappingDropDown + layerID).jqxDropDownList("clearSelection");

        $(".metricParameter" + layerID).jqxInput("clear");
        $(".metricParameter" + layerID).jqxDateTimeInput("clear");

        $(".mappingParameter" + layerID).jqxInput("clear");
        $(".mappingParameter" + layerID).jqxNumberInput("clear");
        $(".mappingParameter" + layerID).jqxDropDownList("clearSelection");

        $(cssIDs.mappingColorDropDown + layerID).jqxDropDownList("clearSelection");

        $(".metricParameter" + layerID).hide();
        $(".mappingParameter" + layerID).hide();
    }

}