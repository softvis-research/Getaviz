class MetricLayer {
    constructor(id) {
        this.id  = id;
        
        this.entities = [];
        
        this.metric = "";
        this.metricFrom = "";
        this.metricTo = "";

        this.mapping = "";
        this.mappingFrom = "";
        this.mappingTo = "";
    }

    buildUILayer(rootDiv) {
        this.buildMetricArea(rootDiv);
        this.buildMappingArea(rootDiv);
    }

    buildMetricArea(rootDiv) {
        var metricTextNode = document.createElement("p");
        metricTextNode.id = "metrics" + this.id;
        metricTextNode.classList.add("metrics");
        metricTextNode.textContent = "Metrics";
        rootDiv.appendChild(metricTextNode);

        var metricDropDownDiv = document.createElement("div");
        metricDropDownDiv.id = domIDs.metricDropDown + this.id;
        metricDropDownDiv.classList.add(domIDs.metricDropDown);
        rootDiv.appendChild(metricDropDownDiv);

        $(cssIDs.metricDropDown + this.id).jqxDropDownList({ 
            source: metricController.controllerConfig.metrics,
            placeHolder: "Select Metric",
            width: 250, height: 30, 
            dropDownVerticalAlignment: "top" 
        });

        $(cssIDs.metricDropDown + this.id).on("change", () => { this.metricDropDownSelected() });

        var metricFromTextNode = document.createElement("p");
        metricFromTextNode.id = domIDs.metricFromText + this.id;
        metricFromTextNode.classList.add("metricParameter");
        metricFromTextNode.classList.add("metricParameter" + this.id);
        metricFromTextNode.textContent = "Metric - From";
        rootDiv.appendChild(metricFromTextNode);

        var metricFromInput = document.createElement("input");
        metricFromInput.type = "number";
        metricFromInput.id = domIDs.metricFromInput + this.id;
        metricFromInput.classList.add("metricParameter");
        metricFromInput.classList.add("metricParameter" + this.id);
        rootDiv.appendChild(metricFromInput);

        $(cssIDs.metricFromInput + this.id).jqxInput({ placeHolder: "From", width: 100, height: 30, minLength: 1 });

        var metricFromDateInput = document.createElement("div");
        metricFromDateInput.id = domIDs.metricFromDateInput + this.id;
        metricFromDateInput.classList.add("metricParameter");
        metricFromDateInput.classList.add("metricParameter" + this.id);
        rootDiv.appendChild(metricFromDateInput);

        $(cssIDs.metricFromDateInput + this.id).jqxDateTimeInput({ 
            placeHolder: "YYYYMMDD", 
            formatString: "yyyyMMdd", 
            value: null, 
            dropDownVerticalAlignment: "top", 
            width: 100, height: 30 
        });


        var metricToTextNode = document.createElement("p");
        metricToTextNode.id = domIDs.metricToText + this.id;
        metricToTextNode.classList.add("metricParameter");
        metricToTextNode.classList.add("metricParameter" + this.id);
        metricToTextNode.textContent = "Metric - To";
        rootDiv.appendChild(metricToTextNode);

        var metricToInput = document.createElement("input");
        metricToInput.type = "number";
        metricToInput.id = domIDs.metricToInput + this.id;
        metricToInput.classList.add("metricParameter");
        rootDiv.appendChild(metricToInput);

        $(cssIDs.metricToInput + this.id).jqxInput({ placeHolder: "To", width: 100, height: 30, minLength: 1 });

        var metricToDateInput = document.createElement("div");
        metricToDateInput.id = domIDs.metricToDateInput + this.id;
        metricToDateInput.classList.add("metricParameter");
        metricToDateInput.classList.add("metricParameter" + this.id);
        rootDiv.appendChild(metricToDateInput);

        $(cssIDs.metricToDateInput + this.id).jqxDateTimeInput({ 
            placeHolder: "YYYYMMDD", 
            formatString: "yyyyMMdd", 
            value: null, 
            dropDownVerticalAlignment: "top", 
            width: 100, height: 30 
        });
    }

    metricDropDownSelected() {
        $(cssIDs.metricFromText + this.id).show();
        $(cssIDs.metricToText + this.id).show();

        switch ($(cssIDs.metricDropDown + this.id).val()) {
            case metrics.numberOfStatements:
                $(cssIDs.metricFromDateInput + this.id).hide();
                $(cssIDs.metricToDateInput + this.id).hide();
                $(cssIDs.metricFromInput + this.id).show();
                $(cssIDs.metricToInput + this.id).show();
                break;
            case metrics.dateOfCreation:
            case metrics.dateOfLastChange:
                $(cssIDs.metricFromInput + this.id).hide();
                $(cssIDs.metricToInput + this.id).hide();
                $(cssIDs.metricFromDateInput + this.id).show();
                $(cssIDs.metricToDateInput + this.id).show();
                break;

        }
    }

    buildMappingArea(rootDiv) {
        var mappingTextNode = document.createElement("p");
        mappingTextNode.id = "mappings" + this.id;
        mappingTextNode.classList.add("mappings");
        mappingTextNode.textContent = "Mappings";
        rootDiv.appendChild(mappingTextNode);

        var mappingDropDownDiv = document.createElement("div");
        mappingDropDownDiv.id = domIDs.mappingDropDown + this.id;
        mappingDropDownDiv.classList.add(domIDs.mappingDropDown);
        rootDiv.appendChild(mappingDropDownDiv);

        $(cssIDs.mappingDropDown + this.id).jqxDropDownList({ 
            source: metricController.controllerConfig.mappings, 
            placeHolder: "Select Mapping", 
            width: 250, height: 30, 
            dropDownVerticalAlignment: "top" 
        });

        $(cssIDs.mappingDropDown + this.id).on("change", () => { this.mappingDropDownSelected() });


        var mappingFromTextNode = document.createElement("p");
        mappingFromTextNode.id = "mappingFromText" + this.id;
        mappingFromTextNode.classList.add("mappingParameter");
        mappingFromTextNode.classList.add("mappingParameter" + this.id);
        mappingFromTextNode.textContent = "Mapping - From";
        rootDiv.appendChild(mappingFromTextNode);

        var mappingFromInput = document.createElement("input");
        mappingFromInput.type = "text";
        mappingFromInput.id = "mappingFromInput" + this.id;
        mappingFromInput.classList.add("mappingParameter");
        mappingFromInput.classList.add("mappingParameter" + this.id);
        rootDiv.appendChild(mappingFromInput);

        $("#mappingFromInput" + this.id).jqxInput({ placeHolder: "From", width: 100, height: 30, minLength: 1 });


        var mappingToTextNode = document.createElement("p");
        mappingToTextNode.id = "mappingToText" + this.id;
        mappingToTextNode.classList.add("mappingParameter");
        mappingToTextNode.classList.add("mappingParameter" + this.id);
        mappingToTextNode.textContent = "Mapping - To";
        rootDiv.appendChild(mappingToTextNode);

        var mappingToInput = document.createElement("input");
        mappingToInput.type = "text";
        mappingToInput.id = "mappingToInput" + this.id;
        mappingToInput.classList.add("mappingParameter");
        mappingToInput.classList.add("mappingParameter" + this.id);
        rootDiv.appendChild(mappingToInput);

        $("#mappingToInput" + this.id).jqxInput({ placeHolder: "To", width: 100, height: 30, minLength: 1 });


        var mappingColorDropDownDiv = document.createElement("div");
        mappingColorDropDownDiv.id = domIDs.mappingColorDropDown + this.id;
        mappingColorDropDownDiv.classList.add("mappingParameter");
        mappingColorDropDownDiv.classList.add("mappingParameter" + this.id);
        rootDiv.appendChild(mappingColorDropDownDiv);

        $(cssIDs.mappingColorDropDown + this.id).jqxDropDownList({ 
            source: colors, 
            placeHolder: "Select Color", 
            width: 250, height: 30, 
            dropDownVerticalAlignment: "top" 
        });


        var mappingStartColorDropDownDiv = document.createElement("div");
        mappingStartColorDropDownDiv.id = domIDs.mappingStartColorDropDown + this.id;        
        mappingStartColorDropDownDiv.classList.add("mappingParameter");
        mappingStartColorDropDownDiv.classList.add("mappingParameter" + this.id);
        rootDiv.appendChild(mappingStartColorDropDownDiv);

        $(cssIDs.mappingStartColorDropDown + this.id).jqxDropDownList({ 
            source: colors, 
            placeHolder: "Select Start Color", 
            width: 250, height: 30, 
            dropDownVerticalAlignment: "top" 
        });

        var mappingEndColorDropDownDiv = document.createElement("div");
        mappingEndColorDropDownDiv.id = domIDs.mappingEndColorDropDown + this.id;
        mappingEndColorDropDownDiv.classList.add("mappingParameter");
        mappingEndColorDropDownDiv.classList.add("mappingParameter" + this.id);
        rootDiv.appendChild(mappingEndColorDropDownDiv);

        $(cssIDs.mappingEndColorDropDown + this.id).jqxDropDownList({ 
            source: colors, 
            placeHolder: "Select End Color", 
            width: 250, height: 30, 
            dropDownVerticalAlignment: "top" });

        
        var transparencyInputDiv = document.createElement("div");
        transparencyInputDiv.id = domIDs.mappingTransparencyInput + this.id;
        transparencyInputDiv.classList.add("mappingParameter");
        transparencyInputDiv.classList.add("mappingParameter" + this.id);
        rootDiv.appendChild(transparencyInputDiv);

        $(cssIDs.mappingTransparencyInput + this.id).jqxNumberInput({ 
            width: 250, height: 30, 
            min: 0, max: 1, 
            inputMode: "simple", 
            spinButtons: true 
        });

        var mappingPeriodTextNode = document.createElement("p");
        mappingPeriodTextNode.id = domIDs.mappingPeriodText + this.id;
        mappingPeriodTextNode.classList.add("mappingParameter");
        mappingPeriodTextNode.classList.add("mappingParameter" + this.id);
        mappingPeriodTextNode.textContent = "Period in ms";
        rootDiv.appendChild(mappingPeriodTextNode);

        var mappingPeriodInput = document.createElement("input");
        mappingPeriodInput.type = "number";
        mappingPeriodInput.id = domIDs.mappingPeriodInput + this.id;
        mappingPeriodInput.classList.add("mappingParameter");
        mappingPeriodInput.classList.add("mappingParameter" + this.id);
        rootDiv.appendChild(mappingPeriodInput);

        $(cssIDs.mappingPeriodInput + this.id).jqxInput({ width: 100, height: 30, minLength: 1 });


        var mappingScaleTextNode = document.createElement("p");
        mappingScaleTextNode.id = domIDs.mappingScaleText + this.id;
        mappingScaleTextNode.classList.add("mappingParameter");
        mappingScaleTextNode.classList.add("mappingParameter" + this.id);
        mappingScaleTextNode.textContent = "Scale";
        rootDiv.appendChild(mappingScaleTextNode);

        var mappingScaleInput = document.createElement("input");
        mappingScaleInput.type = "number";
        mappingScaleInput.id = domIDs.mappingScaleInput + this.id;
        mappingScaleInput.classList.add("mappingParameter");
        mappingScaleInput.classList.add("mappingParameter" + this.id);
        rootDiv.appendChild(mappingScaleInput);

        $(cssIDs.mappingScaleInput + this.id).jqxInput({ width: 100, height: 30, minLength: 1 });
    }

    mappingDropDownSelected() {
        $(".mappingParameter" + this.id).hide();
        
        switch ($(cssIDs.mappingDropDown + this.id).val()) {
            case mappings.color:
                $(cssIDs.mappingColorDropDown + this.id).show();
                break;
            case mappings.colorGradient:
                $(cssIDs.mappingStartColorDropDown + this.id).show();
                $(cssIDs.mappingEndColorDropDown + this.id).show();
                break;
            case mappings.transparency:
                $(cssIDs.mappingTransparencyInput + this.id).show();
                break;
            case mappings.pulsation:
                $(cssIDs.mappingPeriodText + this.id).show();
                $(cssIDs.mappingPeriodInput + this.id).show();
                $(cssIDs.mappingScaleText + this.id).show();
                $(cssIDs.mappingScaleInput + this.id).show();
                break;
            case mappings.flashing:
                $(cssIDs.mappingPeriodText + this.id).show();
                $(cssIDs.mappingPeriodInput + this.id).show();
                $(cssIDs.mappingColorDropDown + this.id).show();
                break;
            case mappings.rotation:
                $(cssIDs.mappingPeriodText + this.id).show();
                $(cssIDs.mappingPeriodInput + this.id).show();
                break;
        }
    }

}