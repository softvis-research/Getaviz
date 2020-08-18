class MetricLayer {
    constructor(id) {
        this.id = id;

        this.entities = [];

        this.entityMetricMap = new Map();

        this.metric = {
            variant: "",
            from: "",
            to: ""
        }

        this.mapping = {
            variant: "",
            color: "",
            startColor: "",
            endColor: "",
            transparency: 0,
            period: 0,
            scale: 0
        };
    }

    buildUILayer(rootDiv) {
        this.buildMetricArea(rootDiv);
        this.buildMappingArea(rootDiv);
        this.buildDeleteButton(rootDiv);
    }

    buildMetricArea(rootDiv) {
        var metricTextNode = document.createElement("p");
        metricTextNode.id = "metrics" + this.id;
        metricTextNode.classList.add("metrics", "layer" + this.id);
        metricTextNode.textContent = "Metrics";
        rootDiv.appendChild(metricTextNode);

        var metricDropDownDiv = document.createElement("div");
        metricDropDownDiv.id = domIDs.metricDropDown + this.id;
        metricDropDownDiv.classList.add(domIDs.metricDropDown, "layer" + this.id);
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
        metricFromTextNode.classList.add("metricParameter", "metricParameter" + this.id, "layer" + this.id);
        metricFromTextNode.textContent = "Metric - From";
        rootDiv.appendChild(metricFromTextNode);

        var metricFromInput = document.createElement("input");
        metricFromInput.type = "number";
        metricFromInput.id = domIDs.metricFromInput + this.id;
        metricFromInput.classList.add("metricParameter", "metricParameter" + this.id, "layer" + this.id);
        rootDiv.appendChild(metricFromInput);

        $(cssIDs.metricFromInput + this.id).jqxInput({ placeHolder: "From", width: 100, height: 30, minLength: 1 });

        var metricFromDateInput = document.createElement("div");
        metricFromDateInput.id = domIDs.metricFromDateInput + this.id;
        metricFromDateInput.classList.add("metricParameter", "metricParameter" + this.id, "layer" + this.id);
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
        metricToTextNode.classList.add("metricParameter", "metricParameter" + this.id, "layer" + this.id);
        metricToTextNode.textContent = "Metric - To";
        rootDiv.appendChild(metricToTextNode);

        var metricToInput = document.createElement("input");
        metricToInput.type = "number";
        metricToInput.id = domIDs.metricToInput + this.id;
        metricToInput.classList.add("metricParameter", "metricParameter" + this.id, "layer" + this.id);
        rootDiv.appendChild(metricToInput);

        $(cssIDs.metricToInput + this.id).jqxInput({ placeHolder: "To", width: 100, height: 30, minLength: 1 });

        var metricToDateInput = document.createElement("div");
        metricToDateInput.id = domIDs.metricToDateInput + this.id;
        metricToDateInput.classList.add("metricParameter", "metricParameter" + this.id, "layer" + this.id);
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
        mappingTextNode.classList.add("mappings", "layer" + this.id);
        mappingTextNode.textContent = "Mappings";
        rootDiv.appendChild(mappingTextNode);

        var mappingDropDownDiv = document.createElement("div");
        mappingDropDownDiv.id = domIDs.mappingDropDown + this.id;
        mappingDropDownDiv.classList.add(domIDs.mappingDropDown, "layer" + this.id);
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
        mappingFromTextNode.classList.add("mappingParameter", "mappingParameter" + this.id, "layer" + this.id);
        mappingFromTextNode.textContent = "Mapping - From";
        rootDiv.appendChild(mappingFromTextNode);

        var mappingFromInput = document.createElement("input");
        mappingFromInput.type = "text";
        mappingFromInput.id = "mappingFromInput" + this.id;
        mappingFromInput.classList.add("mappingParameter", "mappingParameter" + this.id, "layer" + this.id);
        rootDiv.appendChild(mappingFromInput);

        $("#mappingFromInput" + this.id).jqxInput({ placeHolder: "From", width: 100, height: 30, minLength: 1 });


        var mappingToTextNode = document.createElement("p");
        mappingToTextNode.id = "mappingToText" + this.id;
        mappingToTextNode.classList.add("mappingParameter", "mappingParameter" + this.id, "layer" + this.id);
        mappingToTextNode.textContent = "Mapping - To";
        rootDiv.appendChild(mappingToTextNode);

        var mappingToInput = document.createElement("input");
        mappingToInput.type = "text";
        mappingToInput.id = "mappingToInput" + this.id;
        mappingToInput.classList.add("mappingParameter", "mappingParameter" + this.id, "layer" + this.id);
        rootDiv.appendChild(mappingToInput);

        $("#mappingToInput" + this.id).jqxInput({ placeHolder: "To", width: 100, height: 30, minLength: 1 });


        var mappingColorDropDownDiv = document.createElement("div");
        mappingColorDropDownDiv.id = domIDs.mappingColorDropDown + this.id;
        mappingColorDropDownDiv.classList.add("mappingParameter", "mappingParameter" + this.id, "layer" + this.id);
        rootDiv.appendChild(mappingColorDropDownDiv);

        $(cssIDs.mappingColorDropDown + this.id).jqxDropDownList({
            source: colors,
            placeHolder: "Select Color",
            width: 250, height: 30,
            dropDownVerticalAlignment: "top"
        });


        var mappingStartColorDropDownDiv = document.createElement("div");
        mappingStartColorDropDownDiv.id = domIDs.mappingStartColorDropDown + this.id;
        mappingStartColorDropDownDiv.classList.add("mappingParameter", "mappingParameter" + this.id, "layer" + this.id);
        rootDiv.appendChild(mappingStartColorDropDownDiv);

        $(cssIDs.mappingStartColorDropDown + this.id).jqxDropDownList({
            source: colors,
            placeHolder: "Select Start Color",
            width: 250, height: 30,
            dropDownVerticalAlignment: "top"
        });

        var mappingEndColorDropDownDiv = document.createElement("div");
        mappingEndColorDropDownDiv.id = domIDs.mappingEndColorDropDown + this.id;
        mappingEndColorDropDownDiv.classList.add("mappingParameter", "mappingParameter" + this.id, "layer" + this.id);
        rootDiv.appendChild(mappingEndColorDropDownDiv);

        $(cssIDs.mappingEndColorDropDown + this.id).jqxDropDownList({
            source: colors,
            placeHolder: "Select End Color",
            width: 250, height: 30,
            dropDownVerticalAlignment: "top"
        });


        var transparencyInputDiv = document.createElement("div");
        transparencyInputDiv.id = domIDs.mappingTransparencyInput + this.id;
        transparencyInputDiv.classList.add("mappingParameter", "mappingParameter" + this.id, "layer" + this.id);
        rootDiv.appendChild(transparencyInputDiv);

        $(cssIDs.mappingTransparencyInput + this.id).jqxNumberInput({
            width: 250, height: 30,
            min: 0, max: 1,
            inputMode: "simple",
            spinButtons: true
        });

        var mappingPeriodTextNode = document.createElement("p");
        mappingPeriodTextNode.id = domIDs.mappingPeriodText + this.id;
        mappingPeriodTextNode.classList.add("mappingParameter", "mappingParameter" + this.id, "layer" + this.id);
        mappingPeriodTextNode.textContent = "Period in ms";
        rootDiv.appendChild(mappingPeriodTextNode);

        var mappingPeriodInput = document.createElement("input");
        mappingPeriodInput.type = "number";
        mappingPeriodInput.id = domIDs.mappingPeriodInput + this.id;
        mappingPeriodInput.classList.add("mappingParameter", "mappingParameter" + this.id, "layer" + this.id);
        rootDiv.appendChild(mappingPeriodInput);

        $(cssIDs.mappingPeriodInput + this.id).jqxInput({ width: 100, height: 30, minLength: 1 });


        var mappingScaleTextNode = document.createElement("p");
        mappingScaleTextNode.id = domIDs.mappingScaleText + this.id;
        mappingScaleTextNode.classList.add("mappingParameter", "mappingParameter" + this.id, "layer" + this.id);
        mappingScaleTextNode.textContent = "Scale";
        rootDiv.appendChild(mappingScaleTextNode);

        var mappingScaleInput = document.createElement("input");
        mappingScaleInput.type = "number";
        mappingScaleInput.id = domIDs.mappingScaleInput + this.id;
        mappingScaleInput.classList.add("mappingParameter", "mappingParameter" + this.id, "layer" + this.id);
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

    buildDeleteButton(rootDiv) {
        var deleteButtonDiv = document.createElement("div");
        deleteButtonDiv.id = domIDs.deleteButton + this.id;
        deleteButtonDiv.classList.add(domIDs.deleteButton, "layer" + this.id);
        rootDiv.appendChild(deleteButtonDiv);

        $(cssIDs.deleteButton + this.id).jqxButton({ theme: "metro", height: 30, width: 30, imgSrc: "./scripts/Metric/close.png" });
        
        // $(cssIDs.deleteButton + this.id).click(() => this.container.removeLayer());
        $(cssIDs.deleteButton + this.id).click(() => metricController.removeLayer());
    }

    readUIData() {
        this.metric.variant = ($(cssIDs.metricDropDown + this.id).val() == "" ? metricController.metricDefault.variant : $(cssIDs.metricDropDown + this.id).val());

        switch (this.metric.variant) {
            case metrics.numberOfStatements:
                this.metric.from = $(cssIDs.metricFromInput + this.id).val();
                this.metric.to = $(cssIDs.metricToInput + this.id).val();
                break;
            case metrics.dateOfCreation:
            case metrics.dateOfLastChange:
                this.metric.from = $(cssIDs.metricFromDateInput + this.id).val();
                this.metric.to = $(cssIDs.metricToDateInput + this.id).val();
                break;
        }

        this.mapping.variant = ($(cssIDs.mappingDropDown + this.id).val() == "" ? metricController.mappingDefault.variant : $(cssIDs.mappingDropDown + this.id).val());

        switch (this.mapping.variant) {
            case mappings.color:
                this.mapping.color = ($(cssIDs.mappingColorDropDown + this.id).val() == "" ? metricController.mappingDefault.color : $(cssIDs.mappingColorDropDown + this.id).val());
                break;

            case mappings.colorGradient:
                this.mapping.startColor = ($(cssIDs.mappingStartColorDropDown + this.id).val() == "" ? metricController.mappingDefault.startColor : $(cssIDs.mappingStartColorDropDown + this.id).val());
                this.mapping.endColor = ($(cssIDs.mappingEndColorDropDown + this.id).val() == "" ? metricController.mappingDefault.endColor : $(cssIDs.mappingEndColorDropDown + this.id).val());
                break;

            case mappings.transparency:
                this.mapping.transparency = ($(cssIDs.mappingTransparencyInput + this.id).val() == "" ? metricController.mappingDefault.transparency : $(cssIDs.mappingTransparencyInput + this.id).val());
                break;

            case mappings.pulsation:
                this.mapping.period = ($(cssIDs.mappingPeriodInput + this.id).val() == "" ? metricController.mappingDefault.period : $(cssIDs.mappingPeriodInput + this.id).val());
                this.mapping.scale = ($(cssIDs.mappingScaleInput + this.id).val() == "" ? metricController.mappingDefault.scale : $(cssIDs.mappingScaleInput + this.id).val());
                break;

            case mappings.flashing:
                this.mapping.period = ($(cssIDs.mappingPeriodInput + this.id).val() == "" ? metricController.mappingDefault.period : $(cssIDs.mappingPeriodInput + this.id).val());
                this.mapping.color = ($(cssIDs.mappingColorDropDown + this.id).val() == "" ? metricController.mappingDefault.color : $(cssIDs.mappingColorDropDown + this.id).val());
                break;

            case mappings.rotation:
                this.mapping.period = ($(cssIDs.mappingPeriodInput + this.id).val() == "" ? metricController.mappingDefault.period : $(cssIDs.mappingPeriodInput + this.id).val());
                break;
        }
    }

    async getMatchingEntities() {
        var response = await metricController.getNeo4jData(this.buildCypherQuery());

        response[0].data.forEach(element => {
            this.entities.push(model.getEntityById(element.row[0]));
            this.entityMetricMap.set(element.row[0], element.row[1]);
        });
    }

    buildCypherQuery() {
        var cypherQuery = "";

        switch (this.metric.variant) {
            default:
            case metrics.numberOfStatements:
                if (this.metric.from == "" && this.metric.to == "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.number_of_statements > 10 RETURN n.hash, p.number_of_statements";
                else if (this.metric.from != "" && this.metric.to == "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.number_of_statements >= " + this.metric.from + " RETURN n.hash, p.number_of_statements";
                else if (this.metric.from == "" && this.metric.to != "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.number_of_statements <= " + this.metric.to + " RETURN n.hash, p.number_of_statements";
                else if (this.metric.from != "" && this.metric.to != "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where " + this.metric.from + " <= p.number_of_statements <= " + this.metric.to + " RETURN n.hash, p.number_of_statements";
                break;
            case metrics.dateOfCreation:
                if (this.metric.from == "" && this.metric.to == "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.created >= date('20200101') RETURN n.hash, p.created";
                else if (this.metric.from != "" && this.metric.to == "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.created >= date('" + this.metric.from + "') RETURN n.hash, p.created";
                else if (this.metric.from == "" && this.metric.to != "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.created <= date('" + this.metric.to + "') RETURN n.hash, p.created";
                else if (this.metric.from != "" && this.metric.to != "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where date('" + this.metric.from + "') <= p.created <= date('" + this.metric.to + "') RETURN n.hash, p.created";
                break;
            case metrics.dateOfLastChange:
                if (this.metric.from == "" && this.metric.to == "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.changed >= date('20200101') RETURN n.hash, p.changed";
                else if (this.metric.from != "" && this.metric.to == "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.changed >= date('" + this.metric.from + "') RETURN n.hash, p.changed";
                else if (this.metric.from == "" && this.metric.to != "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.changed <= date('" + this.metric.to + "') RETURN n.hash, p.changed";
                else if (this.metric.from != "" && this.metric.to != "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where date('" + this.metric.from + "') <= p.changed <= date('" + this.metric.to + "') RETURN n.hash, p.changed";
                break;
        }

        return cypherQuery;
    }

    doMapping() {
        switch (this.mapping.variant) {
            default:
            case mappings.color:
                canvasManipulator.changeColorOfEntities(this.entities, this.mapping.color);
                break;
            case mappings.colorGradient:
                this.setColorGradient();
                break;
            case mappings.transparency:
                canvasManipulator.changeTransparencyOfEntities(this.entities, this.mapping.transparency);
                break;
            case mappings.pulsation:
                canvasManipulator.startAnimation({ animation: "Expanding", entities: this.entities, period: this.mapping.period, scale: this.mapping.scale });
                break;
            case mappings.flashing:
                canvasManipulator.startAnimation({ animation: "Flashing", entities: this.entities, period: this.mapping.period, flashingColor: this.mapping.color });
                break;
            case mappings.rotation:
                canvasManipulator.startAnimation({ animation: "Rotation", entities: this.entities, period: this.mapping.period });
                break;
        }
    }

    setColorGradient() {
        var minValue;
        var maxValue;

        this.entityMetricMap.forEach(function (metricValue) {
            if (minValue >= metricValue || minValue == undefined) {
                minValue = metricValue;
            }
            if (maxValue <= metricValue || maxValue == undefined) {
                maxValue = metricValue;
            }
        })

        var colorGradient = new ColorGradient(this.mapping.startColor, this.mapping.endColor, minValue, maxValue);

        this.entities.forEach(function (entity) {
            var gradientColor = colorGradient.calculateGradientColor(this.entityMetricMap.get(entity.id));
            canvasManipulator.changeColorOfEntities([entity], gradientColor.r + " " + gradientColor.g + " " + gradientColor.b);
        }, this)

    }

    destroy() {
        this.reset();
        $(".layer" + this.id).remove();
    }

    reset() {
        $(cssIDs.metricDropDown + this.id).jqxDropDownList("clearSelection");
        $(cssIDs.mappingDropDown + this.id).jqxDropDownList("clearSelection");

        $(".metricParameter" + this.id).jqxInput("clear");
        $(".metricParameter" + this.id).jqxDateTimeInput("clear");

        $(".mappingParameter" + this.id).jqxInput("clear");
        $(".mappingParameter" + this.id).jqxNumberInput("clear");
        $(".mappingParameter" + this.id).jqxDropDownList("clearSelection");

        $(cssIDs.mappingColorDropDown + this.id).jqxDropDownList("clearSelection");

        $(".metricParameter" + this.id).hide();
        $(".mappingParameter" + this.id).hide();

        switch (this.mapping.variant) {
            case mappings.color:
                canvasManipulator.resetColorOfEntities(this.entities);
                break;
            case mappings.colorGradient:
                canvasManipulator.resetColorOfEntities(this.entities);
                break;
            case mappings.transparency:
                canvasManipulator.resetTransparencyOfEntities(this.entities);
                break;
            case mappings.pulsation:
                canvasManipulator.stopAnimation({ animation: "Expanding", entities: this.entities });
                break;
            case mappings.flashing:
                canvasManipulator.stopAnimation({ animation: "Flashing", entities: this.entities });
                break;
            case mappings.rotation:
                canvasManipulator.stopAnimation({ animation: "Rotation", entities: this.entities });
                break;
        }

        this.entities = [];
        this.entityMetricMap = new Map();
    }

}