var metricController = (function () {

    var controllerConfig = {
        metrics: [
            metrics.numberOfStatements,
            metrics.dateOfCreation,
            metrics.dateOfLastChange
        ],
        mappings: [
            mappings.color,
            mappings.colorGradient,
            mappings.transparency,
            mappings.pulsation,
            mappings.flashing,
            mappings.rotation,
        ]
    };

    var domHelper = new DomHelper();

    var metric = {
        variant: "",
        from: 0,
        to: 0
    };

    var metricDefault = {
        variant: metrics.numberOfStatements,
        from: 0,
        to: 0
    }

    var mapping = {
        variant: "",
        color: "",
        startColor: "",
        endColor: "",
        transparency: 0,
        period: 0,
        scale: 0
    };

    var mappingDefault = {
        variant: mappings.color,
        color: "black",
        startColor: "blue",
        endColor: "red",
        transparency: 0.5,
        period: 1000,
        scale: 2
    };

    var entities = [];
    var entityMetricMap = new Map();

    function initialize(setupConfig) {
        application.transferConfigParams(setupConfig, controllerConfig);
    }

    function activate(rootDiv) {
        domHelper.buildUI(rootDiv, controllerConfig);

        $(cssIDs.executeButton).click(executeButtonClicked);
        $(cssIDs.resetButton).click(reset);
    }

    async function executeButtonClicked(event) {

        readUIData();

        await getMatchingEntities(buildCypherQuery());

        doMapping();
    }

    function buildCypherQuery() {
        var cypherQuery = "";

        switch (metric.variant) {
            default:
            case metrics.numberOfStatements:
                if (metric.from == "" && metric.to == "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.number_of_statements > 10 RETURN n.hash, p.number_of_statements";
                else if (metric.from != "" && metric.to == "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.number_of_statements >= " + metric.from + " RETURN n.hash, p.number_of_statements";
                else if (metric.from == "" && metric.to != "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.number_of_statements <= " + metric.to + " RETURN n.hash, p.number_of_statements";
                else if (metric.from != "" && metric.to != "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where " + metric.from + " <= p.number_of_statements <= " + metric.to + " RETURN n.hash, p.number_of_statements";
                break;
            case metrics.dateOfCreation:
                if (metric.from == "" && metric.to == "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.created >= date('20200101') RETURN n.hash, p.created";
                else if (metric.from != "" && metric.to == "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.created >= date('" + metric.from + "') RETURN n.hash, p.created";
                else if (metric.from == "" && metric.to != "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.created <= date('" + metric.to + "') RETURN n.hash, p.created";
                else if (metric.from != "" && metric.to != "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where date('" + metric.from + "') <= p.created <= date('" + metric.to + "') RETURN n.hash, p.created";
                break;
            case metrics.dateOfLastChange:
                if (metric.from == "" && metric.to == "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.changed >= date('20200101') RETURN n.hash, p.changed";
                else if (metric.from != "" && metric.to == "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.changed >= date('" + metric.from + "') RETURN n.hash, p.changed";
                else if (metric.from == "" && metric.to != "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.changed <= date('" + metric.to + "') RETURN n.hash, p.changed";
                else if (metric.from != "" && metric.to != "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where date('" + metric.from + "') <= p.changed <= date('" + metric.to + "') RETURN n.hash, p.changed";
                break;
        }

        return cypherQuery;
    }

    async function getMatchingEntities(cypherQuery) {
        var response = await getNeo4jData(cypherQuery);

        response[0].data.forEach(element => {
            entities.push(model.getEntityById(element.row[0]));
            entityMetricMap.set(element.row[0], element.row[1]);
        });
    }

    function doMapping() {

        switch (mapping.variant) {
            default:
            case mappings.color:
                canvasManipulator.changeColorOfEntities(entities, mapping.color);
                break;
            case mappings.colorGradient:
                setColorGradient();
                break;
            case mappings.transparency:
                canvasManipulator.changeTransparencyOfEntities(entities, mapping.transparency);
                break;
            case mappings.pulsation:
                canvasManipulator.startAnimation({ animation: "Expanding", entities: entities, period: mapping.period, scale: mapping.scale });
                break;
            case mappings.flashing:
                canvasManipulator.startAnimation({ animation: "Flashing", entities: entities, period: mapping.period, flashingColor: mapping.color });
                break;
            case mappings.rotation:
                canvasManipulator.startAnimation({ animation: "Rotation", entities: entities, period: mapping.period });
                break;
        }


    }

    // Universal method to load a data from Neo4j using imported cypher-query
    async function getNeo4jData(cypherQuery) {
        const payload = {
            'statements': [
                // neo4j requires keyword "statement", so leave as is
                { 'statement': `${cypherQuery}` }
            ]
        }

        try {
            let response = await fetch('http://localhost:7474/db/data/transaction/commit', {
                method: 'POST',
                body: JSON.stringify(payload),
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            let data = await response.json();
            return data.results;
        } catch (error) {
            events.log.warning.publish({ text: error });
        }
    }

    function readUIData() {
        metric.variant = ($(cssIDs.metricDropDown).val() == "" ? metricDefault.variant : $(cssIDs.metricDropDown).val());

        switch (metric.variant) {
            case metrics.numberOfStatements:
                metric.from = $(cssIDs.metricFromInput).val();
                metric.to = $(cssIDs.metricToInput).val();
                break;
            case metrics.dateOfCreation:
            case metrics.dateOfLastChange:
                metric.from = $(cssIDs.metricFromDateInput).val();
                metric.to = $(cssIDs.metricToDateInput).val();
                break;
        }

        mapping.variant = ($(cssIDs.mappingDropDown).val() == "" ? mappingDefault.variant : $(cssIDs.mappingDropDown).val());

        switch (mapping.variant) {
            case mappings.color:
                mapping.color = ($(cssIDs.mappingColorDropDown).val() == "" ? mappingDefault.color : $(cssIDs.mappingColorDropDown).val());
                break;

            case mappings.colorGradient:
                mapping.startColor = ($(cssIDs.mappingStartColorDropDown).val() == "" ? mappingDefault.startColor : $(cssIDs.mappingStartColorDropDown).val());
                mapping.endColor = ($(cssIDs.mappingEndColorDropDown).val() == "" ? mappingDefault.endColor : $(cssIDs.mappingEndColorDropDown).val());
                break;

            case mappings.transparency:
                mapping.transparency = ($(cssIDs.mappingTransparencyInput).val() == "" ? mappingDefault.transparency : $(cssIDs.mappingTransparencyInput).val());
                break;

            case mappings.pulsation:
                mapping.period = ($(cssIDs.mappingPeriodInput).val() == "" ? mappingDefault.period : $(cssIDs.mappingPeriodInput).val());
                mapping.scale = ($(cssIDs.mappingScaleInput).val() == "" ? mappingDefault.scale : $(cssIDs.mappingScaleInput).val());
                break;

            case mappings.flashing:
                mapping.period = ($(cssIDs.mappingPeriodInput).val() == "" ? mappingDefault.period : $(cssIDs.mappingPeriodInput).val());
                mapping.color = ($(cssIDs.mappingColorDropDown).val() == "" ? mappingDefault.color : $(cssIDs.mappingColorDropDown).val());
                break;

            case mappings.rotation:
                mapping.period = ($(cssIDs.mappingPeriodInput).val() == "" ? mappingDefault.period : $(cssIDs.mappingPeriodInput).val());
                break;
        }
    }

    function setColorGradient() {
        var minValue;
        var maxValue;

        entityMetricMap.forEach(function(metricValue) {
            if (minValue >= metricValue || minValue == undefined) {
                minValue = metricValue;
            }
            if (maxValue <= metricValue || maxValue == undefined) {
                maxValue = metricValue;
            }
        })

        var colorGradient = new ColorGradient(mapping.startColor, mapping.endColor, minValue, maxValue);

        entities.forEach(function(entity) {
            var gradientColor = colorGradient.calculateGradientColor(entityMetricMap.get(entity.id));
            canvasManipulator.changeColorOfEntities([entity], gradientColor.r + " " + gradientColor.g + " " + gradientColor.b);
        })

    }

    function reset() {
        switch (mapping.variant) {
            case mappings.color:
                canvasManipulator.resetColorOfEntities(entities);
                break;
            case mappings.colorGradient:
                canvasManipulator.resetColorOfEntities(entities);
                break;
            case mappings.transparency:
                canvasManipulator.resetTransparencyOfEntities(entities);
                break;
            case mappings.pulsation:
                canvasManipulator.stopAnimation({ animation: "Expanding", entities: entities });
                break;
            case mappings.flashing:
                canvasManipulator.stopAnimation({ animation: "Flashing", entities: entities });
                break;
            case mappings.rotation:
                canvasManipulator.stopAnimation({ animation: "Rotation", entities: entities });
                break;
        }
        domHelper.resetUI();
        entities = [];
        entityMetricMap = new Map();
    }


    return {
        initialize: initialize,
        activate: activate,
        reset: reset
    }

})();