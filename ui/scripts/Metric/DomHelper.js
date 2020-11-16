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

        $(cssIDs.executeButton).jqxButton({ theme: "metro", height: widgetSize.buttonHeight, width: "32%" });

        var resetButtonDiv = document.createElement("div");
        resetButtonDiv.id = domIDs.resetButton;
        resetButtonDiv.textContent = "Reset";
        this.rootDiv.appendChild(resetButtonDiv);

        $(cssIDs.resetButton).jqxButton({ theme: "metro", height: widgetSize.buttonHeight, width: "32%" });

        var addLayerButtonDiv = document.createElement("div");
        addLayerButtonDiv.id = domIDs.addLayerButton;
        addLayerButtonDiv.textContent = "Add Metric-Layer";
        this.rootDiv.appendChild(addLayerButtonDiv);

        $(cssIDs.addLayerButton).jqxButton({ theme: "metro", height: widgetSize.buttonHeight, width: "32%" });

    }

    buildUiLayer(layerID) {
        this.buildMetricArea(layerID);
        this.buildMappingArea(layerID);
        this.buildDeleteButton(layerID);
    }

    buildMetricArea(layerID) {
        var metricTextNode = document.createElement("label");
        metricTextNode.id = domIDs.metrics + layerID;
        metricTextNode.classList.add(domClasses.metrics, domClasses.layer + layerID, domClasses.textLabel);
        metricTextNode.textContent = "Metrics";
        this.rootDiv.appendChild(metricTextNode);

        var metricDropDownDiv = document.createElement("div");
        metricDropDownDiv.id = domIDs.metricDropDown + layerID;
        metricDropDownDiv.classList.add(domClasses.metricDropDown, domClasses.layer + layerID);
        this.rootDiv.appendChild(metricDropDownDiv);

        $(cssIDs.metricDropDown + layerID).jqxDropDownList({
            source: this.controllerConfig.metrics,
            placeHolder: "Select Metric",
            width: widgetSize.dropDownWidth, height: widgetSize.dropDownHeight,
            dropDownVerticalAlignment: "top",
            autoDropDownHeight: true,
            autoItemsHeight: true
        });

        $(cssIDs.metricDropDown + layerID).on("change", () => { this.metricDropDownSelected(layerID) });

        var metricFromTextNode = document.createElement("label");
        metricFromTextNode.id = domIDs.metricFromText + layerID;
        metricFromTextNode.classList.add(domClasses.metricParameter, domClasses.metricParameter + layerID, domClasses.layer + layerID, domClasses.textLabel);
        metricFromTextNode.textContent = "From";
        this.rootDiv.appendChild(metricFromTextNode);

        var metricFromInput = document.createElement("input");
        metricFromInput.type = "number";
        metricFromInput.id = domIDs.metricFromInput + layerID;
        metricFromInput.classList.add(domClasses.metricParameter, domClasses.metricParameter + layerID, domClasses.layer + layerID);
        this.rootDiv.appendChild(metricFromInput);

        $(cssIDs.metricFromInput + layerID).jqxInput({ placeHolder: "From", width: widgetSize.inputWidth, height: widgetSize.inputHeight, minLength: 1 });

        var metricFromDateInput = document.createElement("div");
        metricFromDateInput.id = domIDs.metricFromDateInput + layerID;
        metricFromDateInput.classList.add(domClasses.metricParameter, domClasses.metricParameter + layerID, domClasses.layer + layerID);
        this.rootDiv.appendChild(metricFromDateInput);

        $(cssIDs.metricFromDateInput + layerID).jqxDateTimeInput({
            placeHolder: "YYYY-MM-DD",
            formatString: "yyyy-MM-dd",
            value: null,
            dropDownVerticalAlignment: "top",
            width: widgetSize.inputWidth, height: widgetSize.inputHeight
        });


        var metricToTextNode = document.createElement("label");
        metricToTextNode.id = domIDs.metricToText + layerID;
        metricToTextNode.classList.add(domClasses.metricParameter, domClasses.metricParameter + layerID, domClasses.layer + layerID, domClasses.textLabel);
        metricToTextNode.textContent = "To";
        this.rootDiv.appendChild(metricToTextNode);

        var metricToInput = document.createElement("input");
        metricToInput.type = "number";
        metricToInput.id = domIDs.metricToInput + layerID;
        metricToInput.classList.add(domClasses.metricParameter, domClasses.metricParameter + layerID, domClasses.layer + layerID);
        this.rootDiv.appendChild(metricToInput);

        $(cssIDs.metricToInput + layerID).jqxInput({ placeHolder: "To", width: widgetSize.inputWidth, height: widgetSize.inputHeight, minLength: 1 });

        var metricToDateInput = document.createElement("div");
        metricToDateInput.id = domIDs.metricToDateInput + layerID;
        metricToDateInput.classList.add(domClasses.metricParameter, domClasses.metricParameter + layerID, domClasses.layer + layerID);
        this.rootDiv.appendChild(metricToDateInput);

        $(cssIDs.metricToDateInput + layerID).jqxDateTimeInput({
            placeHolder: "YYYY-MM-DD",
            formatString: "yyyy-MM-dd",
            value: null,
            dropDownVerticalAlignment: "top",
            width: widgetSize.inputWidth, height: widgetSize.inputHeight
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
        var mappingTextNode = document.createElement("label");
        mappingTextNode.id = domIDs.mappings + layerID;
        mappingTextNode.classList.add(domClasses.mappings, domClasses.layer + layerID, domClasses.textLabel);
        mappingTextNode.textContent = "Mappings";
        this.rootDiv.appendChild(mappingTextNode);

        var mappingDropDownDiv = document.createElement("div");
        mappingDropDownDiv.id = domIDs.mappingDropDown + layerID;
        mappingDropDownDiv.classList.add(domClasses.mappingDropDown, domClasses.layer + layerID);
        this.rootDiv.appendChild(mappingDropDownDiv);

        $(cssIDs.mappingDropDown + layerID).jqxDropDownList({
            source: this.controllerConfig.mappings,
            placeHolder: "Select Mapping",
            width: widgetSize.dropDownWidth, height: widgetSize.dropDownHeight,
            dropDownVerticalAlignment: "top",
            autoDropDownHeight: true,
            autoItemsHeight: true
        });

        $(cssIDs.mappingDropDown + layerID).on("change", () => { this.mappingDropDownSelected(layerID) });


        var mappingFromTextNode = document.createElement("label");
        mappingFromTextNode.id = domIDs.mappingFromText + layerID;
        mappingFromTextNode.classList.add(domClasses.mappingParameter, domClasses.mappingParameter + layerID, domClasses.layer + layerID, domClasses.textLabel);
        mappingFromTextNode.textContent = "Mapping - From";
        this.rootDiv.appendChild(mappingFromTextNode);

        var mappingFromInput = document.createElement("input");
        mappingFromInput.type = "text";
        mappingFromInput.id = domIDs.mappingFromInput + layerID;
        mappingFromInput.classList.add(domClasses.mappingParameter, domClasses.mappingParameter + layerID, domClasses.layer + layerID);
        this.rootDiv.appendChild(mappingFromInput);

        $(cssIDs.mappingFromInput + layerID).jqxInput({ placeHolder: "From", width: widgetSize.inputWidth, height: widgetSize.inputHeight, minLength: 1 });


        var mappingToTextNode = document.createElement("label");
        mappingToTextNode.id = domIDs.mappingToText + layerID;
        mappingToTextNode.classList.add(domClasses.mappingParameter, domClasses.mappingParameter + layerID, domClasses.layer + layerID, domClasses.textLabel);
        mappingToTextNode.textContent = "Mapping - To";
        this.rootDiv.appendChild(mappingToTextNode);

        var mappingToInput = document.createElement("input");
        mappingToInput.type = "text";
        mappingToInput.id = domIDs.mappingToInput + layerID;
        mappingToInput.classList.add(domClasses.mappingParameter, domClasses.mappingParameter + layerID, domClasses.layer + layerID);
        this.rootDiv.appendChild(mappingToInput);

        $(cssIDs.mappingToInput + layerID).jqxInput({ placeHolder: "To", width: widgetSize.inputWidth, height: widgetSize.inputHeight, minLength: 1 });


        var mappingColorDropDownDiv = document.createElement("div");
        mappingColorDropDownDiv.id = domIDs.mappingColorDropDown + layerID;
        mappingColorDropDownDiv.classList.add(domClasses.mappingParameter, domClasses.mappingParameter + layerID, domClasses.layer + layerID);
        this.rootDiv.appendChild(mappingColorDropDownDiv);

        $(cssIDs.mappingColorDropDown + layerID).jqxDropDownList({
            source: colors,
            placeHolder: "Select Color",
            width: widgetSize.dropDownWidth, height: widgetSize.dropDownHeight,
            dropDownVerticalAlignment: "top",
            autoDropDownHeight: true,
            autoItemsHeight: true
        });


        var mappingStartColorDropDownDiv = document.createElement("div");
        mappingStartColorDropDownDiv.id = domIDs.mappingStartColorDropDown + layerID;
        mappingStartColorDropDownDiv.classList.add(domClasses.mappingParameter, domClasses.mappingParameter + layerID, domClasses.layer + layerID);
        this.rootDiv.appendChild(mappingStartColorDropDownDiv);

        $(cssIDs.mappingStartColorDropDown + layerID).jqxDropDownList({
            source: colors,
            placeHolder: "Select Start Color",
            width: widgetSize.dropDownWidth, height: widgetSize.dropDownHeight,
            dropDownVerticalAlignment: "top",
            autoDropDownHeight: true,
            autoItemsHeight: true
        });

        var mappingEndColorDropDownDiv = document.createElement("div");
        mappingEndColorDropDownDiv.id = domIDs.mappingEndColorDropDown + layerID;
        mappingEndColorDropDownDiv.classList.add(domClasses.mappingParameter, domClasses.mappingParameter + layerID, domClasses.layer + layerID);
        this.rootDiv.appendChild(mappingEndColorDropDownDiv);

        $(cssIDs.mappingEndColorDropDown + layerID).jqxDropDownList({
            source: colors,
            placeHolder: "Select End Color",
            width: widgetSize.dropDownWidth, height: widgetSize.dropDownHeight,
            dropDownVerticalAlignment: "top",
            autoDropDownHeight: true,
            autoItemsHeight: true
        });


        var transparencyInputDiv = document.createElement("div");
        transparencyInputDiv.id = domIDs.mappingTransparencyInput + layerID;
        transparencyInputDiv.classList.add(domClasses.mappingParameter, domClasses.mappingParameter + layerID, domClasses.layer + layerID);
        this.rootDiv.appendChild(transparencyInputDiv);

        $(cssIDs.mappingTransparencyInput + layerID).jqxNumberInput({
            width: 100, height: widgetSize.inputHeight,
            min: 0, max: 1,
            inputMode: "simple",
            spinButtons: true
        });

        var mappingPeriodTextNode = document.createElement("label");
        mappingPeriodTextNode.id = domIDs.mappingPeriodText + layerID;
        mappingPeriodTextNode.classList.add(domClasses.mappingParameter, domClasses.mappingParameter + layerID, domClasses.layer + layerID, domClasses.textLabel);
        mappingPeriodTextNode.textContent = "Period in ms";
        this.rootDiv.appendChild(mappingPeriodTextNode);

        var mappingPeriodInput = document.createElement("input");
        mappingPeriodInput.type = "number";
        mappingPeriodInput.id = domIDs.mappingPeriodInput + layerID;
        mappingPeriodInput.classList.add(domClasses.mappingParameter, domClasses.mappingParameter + layerID, domClasses.layer + layerID);
        this.rootDiv.appendChild(mappingPeriodInput);

        $(cssIDs.mappingPeriodInput + layerID).jqxInput({ width: widgetSize.inputWidth, height: widgetSize.inputHeight, minLength: 1 });


        var mappingScaleTextNode = document.createElement("label");
        mappingScaleTextNode.id = domIDs.mappingScaleText + layerID;
        mappingScaleTextNode.classList.add(domClasses.mappingParameter, domClasses.mappingParameter + layerID, domClasses.layer + layerID, domClasses.textLabel);
        mappingScaleTextNode.textContent = "Scale";
        this.rootDiv.appendChild(mappingScaleTextNode);

        var mappingScaleInput = document.createElement("input");
        mappingScaleInput.type = "number";
        mappingScaleInput.id = domIDs.mappingScaleInput + layerID;
        mappingScaleInput.classList.add(domClasses.mappingParameter, domClasses.mappingParameter + layerID, domClasses.layer + layerID);
        this.rootDiv.appendChild(mappingScaleInput);

        $(cssIDs.mappingScaleInput + layerID).jqxInput({ width: widgetSize.inputWidth, height: widgetSize.inputHeight, minLength: 1 });
    }

    mappingDropDownSelected(layerID) {
        $(cssClasses.mappingParameter + layerID).hide();

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
        deleteButtonDiv.classList.add(domClasses.deleteButton, domClasses.layer + layerID);
        this.rootDiv.appendChild(deleteButtonDiv);

        $(cssIDs.deleteButton + layerID).jqxButton({ 
            theme: "metro", 
            height: widgetSize.deleteButtonHeight, 
            width: widgetSize.deleteButtonHeight, 
            imgSrc: "./scripts/Metric/close.png" 
        });

        $(cssIDs.deleteButton + layerID).click((event) => metricController.removeLayer(event));
    }

    destroyLayerUI(layerID) {
        this.resetLayerUI(layerID);
        $(cssClasses.layer + layerID).remove();
    }

    resetLayerUI(layerID) {
        $(cssIDs.metricDropDown + layerID).jqxDropDownList("clearSelection");
        $(cssIDs.mappingDropDown + layerID).jqxDropDownList("clearSelection");

        $(cssClasses.metricParameter + layerID).jqxInput("clear");
        $(cssClasses.metricParameter + layerID).jqxDateTimeInput("clear");

        $(cssClasses.mappingParameter + layerID).jqxInput("clear");
        $(cssClasses.mappingParameter + layerID).jqxNumberInput("clear");
        $(cssClasses.mappingParameter + layerID).jqxDropDownList("clearSelection");

        $(cssIDs.mappingColorDropDown + layerID).jqxDropDownList("clearSelection");

        $(cssClasses.metricParameter + layerID).hide();
        $(cssClasses.mappingParameter + layerID).hide();
    }

}