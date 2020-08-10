class DomHelper {
    
    constructor() {
        this.colors = [
            "red",
            "blue",
            "green",
            "black",
            "yellow",
            "orange"
        ]
    }

    buildUI(rootDiv, controllerConfig) {
        let cssLink = document.createElement('link');
        cssLink.type = 'text/css';
        cssLink.rel = 'stylesheet';
        cssLink.href = 'scripts/Metric/metricBox.css';
        document.getElementsByTagName('head')[0].appendChild(cssLink);

        this.buildMetricArea(rootDiv, controllerConfig);

        this.buildMappingArea(rootDiv, controllerConfig);

        var executeButtonDiv = document.createElement("div");
        executeButtonDiv.id = "executeButton";
        executeButtonDiv.textContent = "Execute";
        rootDiv.appendChild(executeButtonDiv);

        $("#executeButton").jqxButton({ theme: "metro", height: 20, width: "45%" });

        var resetButtonDiv = document.createElement("div");
        resetButtonDiv.id = "resetButton";
        resetButtonDiv.textContent = "Reset";
        rootDiv.appendChild(resetButtonDiv);

        $("#resetButton").jqxButton({ theme: "metro", height: 20, width: "45%" });
    }

    buildMetricArea(rootDiv, controllerConfig) {
        var metricTextNode = document.createElement("p");
        metricTextNode.id = "metrics";
        metricTextNode.textContent = "Metrics";
        rootDiv.appendChild(metricTextNode);

        var metricDropDownDiv = document.createElement("div");
        metricDropDownDiv.id = "metricDropDown";
        rootDiv.appendChild(metricDropDownDiv);

        $("#metricDropDown").jqxDropDownList({ source: controllerConfig.metrics, placeHolder: "Select Metric", width: 250, height: 30, dropDownVerticalAlignment: "top" });

        $("#metricDropDown").on("change", this.metricDropDownSelected);

        var metricFromTextNode = document.createElement("p");
        metricFromTextNode.id = "metricFromText";
        metricFromTextNode.classList.add("metricParameter");
        metricFromTextNode.textContent = "Metric - From";
        rootDiv.appendChild(metricFromTextNode);

        var metricFromInput = document.createElement("input");
        metricFromInput.type = "text";
        metricFromInput.id = "metricFromInput";
        metricFromInput.classList.add("metricParameter");
        rootDiv.appendChild(metricFromInput);

        $("#metricFromInput").jqxInput({ placeHolder: "From", width: 100, height: 30, minLength: 1 });

        var metricFromDateInput = document.createElement("div");
        metricFromDateInput.type = "text";
        metricFromDateInput.id = "metricFromDateInput";
        metricFromDateInput.classList.add("metricParameter");
        rootDiv.appendChild(metricFromDateInput);

        $("#metricFromDateInput").jqxDateTimeInput({ placeHolder: "YYYYMMDD", formatString: "yyyyMMdd", value: null, dropDownVerticalAlignment: "top", width: 100, height: 30 });
        

        var metricToTextNode = document.createElement("p");
        metricToTextNode.id = "metricToText";
        metricToTextNode.classList.add("metricParameter");
        metricToTextNode.textContent = "Metric - To";
        rootDiv.appendChild(metricToTextNode);

        var metricToInput = document.createElement("input");
        metricToInput.type = "text";
        metricToInput.id = "metricToInput";
        metricToInput.classList.add("metricParameter");
        rootDiv.appendChild(metricToInput);

        $("#metricToInput").jqxInput({ placeHolder: "To", width: 100, height: 30, minLength: 1 });

        var metricToDateInput = document.createElement("div");
        metricToDateInput.type = "text";
        metricToDateInput.id = "metricToDateInput";
        metricToDateInput.classList.add("metricParameter");
        rootDiv.appendChild(metricToDateInput);

       $("#metricToDateInput").jqxDateTimeInput({ placeHolder: "YYYYMMDD", formatString: "yyyyMMdd", value: null, dropDownVerticalAlignment: "top", width: 100, height: 30 });
        
    }

    metricDropDownSelected() {
        switch($("#metricDropDown").val()) {
            case "Number of Statements":
                $("#metricFromDateInput").hide();
                $("#metricToDateInput").hide();
                $("#metricFromInput").show();
                $("#metricToInput").show();
                break;
            case "Date of Creation":                
            case "Date of Last Change":
                $("#metricFromInput").hide();
                $("#metricToInput").hide();
                $("#metricFromDateInput").show();
                $("#metricToDateInput").show();
                break;
                
        }
    }

    buildMappingArea(rootDiv, controllerConfig) {
        var mappingTextNode = document.createElement("p");
        mappingTextNode.id = "mappings";
        mappingTextNode.textContent = "Mappings";
        rootDiv.appendChild(mappingTextNode);

        var mappingDropDownDiv = document.createElement("div");
        mappingDropDownDiv.id = "mappingDropDown";
        rootDiv.appendChild(mappingDropDownDiv);

        $("#mappingDropDown").jqxDropDownList({ source: controllerConfig.mappings, placeHolder: "Select Mapping", width: 250, height: 30, dropDownVerticalAlignment: "top" });

        $("#mappingDropDown").on("change", this.mappingDropDownSelected);
        
        
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
        mappingColorDropDownDiv.id = "mappingColorDropDown";
        mappingColorDropDownDiv.classList.add("mappingParameter");
        rootDiv.appendChild(mappingColorDropDownDiv);

        $("#mappingColorDropDown").jqxDropDownList({ source: this.colors, placeHolder: "Select Color", width: 250, height: 30, dropDownVerticalAlignment: "top" });

        var transparencyInputDiv = document.createElement("div");
        transparencyInputDiv.id = "mappingTransparencyInput";
        transparencyInputDiv.classList.add("mappingParameter");
        rootDiv.appendChild(transparencyInputDiv);

        $("#mappingTransparencyInput").jqxNumberInput({ width: 250, height: 30, min: 0, max: 1, inputMode: "simple", spinButtons: true });
        

    }

    mappingDropDownSelected() {
        $(".mappingParameter").hide();
        switch($("#mappingDropDown").val()) {
            case "Color":
                $("#mappingColorDropDown").show();
                break;
            case "Transparency":
                $("#mappingTransparencyInput").show();
                break;
            case "Pulsation":
            case "Flashing":
            case "Rotation":
                $("#mappingFromInput").show();
                $("#mappingToInput").show();
        }
    }

    resetUI() {
        $("#metricDropDown").jqxDropDownList("clearSelection");
        $("#mappingDropDown").jqxDropDownList("clearSelection");
        $("#mappingColorDropDown").jqxDropDownList("clearSelection");
        
        $("#metricFromInput").jqxInput("clear");
        $("#metricFromDateInput").jqxInput("clear");
        $("#metricToInput").jqxInput("clear");
        $("#metricToDateInput").jqxInput("clear");
        
        $("#mappingFromInput").jqxInput("clear");
        $("#mappingToInput").jqxInput("clear");
        
        $(".metricParameter").hide();
        $(".mappingParameter").hide();
    }


}