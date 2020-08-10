var metricController = (function () {

    var controllerConfig = {
        metrics: [
            "Number of Statements",
            "Date of Creation",
            "Date of Last Change"
        ],
        mappings: [
            "Color",
            "Transparency",
            "Pulsation",
            "Flashing",
            "Rotation",
        ]
    };

    var domHelper = new DomHelper();

    var metric = "Number of Statements";
    var metricFrom = 0;
    var metricTo = 0;

    var mapping = "Color";
    var mappingFrom = "";
    var mappingTo = "";

    var entities = [];

    function initialize(setupConfig) {
        application.transferConfigParams(setupConfig, controllerConfig);
    }

    function activate(rootDiv) {
        domHelper.buildUI(rootDiv, controllerConfig);

        $("#executeButton").click(executeButtonClicked);
        $("#resetButton").click(reset);
    }

    async function executeButtonClicked(event) {

        metric = $("#metricDropDown").val();

        switch (metric) {
            case "Number of Statements":
                metricFrom = $("#metricFromInput").val();
                metricTo = $("#metricToInput").val();
                break;
            case "Date of Creation":
            case "Date of Last Change":
                metricFrom = $("#metricFromDateInput").val();
                metricTo = $("#metricToDateInput").val();
                break;
        }



        mapping = $("#mappingDropDown").val();
        mappingFrom = $("#mappingFromInput").val();
        mappingTo = $("#mappingToInput").val();


        await getMatchingEntities(buildCypherQuery());

        doMapping();
    }

    function buildCypherQuery() {
        var cypherQuery = "";

        switch (metric) {
            default:
            case "Number of Statements":
                if (metricFrom == "" && metricTo == "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.number_of_statements > 10 RETURN n.hash";
                else if (metricFrom != "" && metricTo == "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.number_of_statements >= " + metricFrom + " RETURN n.hash";
                else if (metricFrom == "" && metricTo != "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.number_of_statements <= " + metricTo + " RETURN n.hash";
                else if (metricFrom != "" && metricTo != "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where " + metricFrom + " <= p.number_of_statements <= " + metricTo + " RETURN n.hash";
                break;
            case "Date of Creation":
                if (metricFrom == "" && metricTo == "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.created >= date('20200101') RETURN n.hash";
                else if (metricFrom != "" && metricTo == "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.created >= date('" + metricFrom + "') RETURN n.hash";
                else if (metricFrom == "" && metricTo != "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.created <= date('" + metricTo + "') RETURN n.hash";
                else if (metricFrom != "" && metricTo != "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where date('" + metricFrom + "') <= p.created <= date('" + metricTo + "') RETURN n.hash";
                break;
            case "Date of Last Change":
                if (metricFrom == "" && metricTo == "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.changed >= date('20200101') RETURN n.hash";
                else if (metricFrom != "" && metricTo == "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.changed >= date('" + metricFrom + "') RETURN n.hash";
                else if (metricFrom == "" && metricTo != "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where p.changed <= date('" + metricTo + "') RETURN n.hash";
                else if (metricFrom != "" && metricTo != "")
                    cypherQuery = "MATCH (n)-[:SOURCE]->(p) where date('" + metricFrom + "') <= p.changed <= date('" + metricTo + "') RETURN n.hash";
                break;
        }

        return cypherQuery;
    }

    async function getMatchingEntities(cypherQuery) {
        var response = await getNeo4jData(cypherQuery);

        response[0].data.forEach(element => {
            entities.push(model.getEntityById(element.row[0]));
        });
    }

    function doMapping() {

        switch (mapping) {
            default:
            case "Color":
                mappingFrom = $("#mappingColorDropDown").val();
                if (mappingFrom == "")
                    canvasManipulator.changeColorOfEntities(entities, "black");
                else
                    canvasManipulator.changeColorOfEntities(entities, mappingFrom);
                break;
            case "Transparency":
                mappingFrom = $("#mappingTransparencyInput").val();
                if (mappingFrom == "")
                    canvasManipulator.changeTransparencyOfEntities(entities, 0.5);
                else
                    canvasManipulator.changeTransparencyOfEntities(entities, mappingFrom);
                break;
            case "Pulsation":
                break;
            case "Flashing":
                break;
            case "Rotation":
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

    function reset() {
        switch (mapping) {
            case "Color":
                canvasManipulator.resetColorOfEntities(entities);
                break;
            case "Transparency":
                canvasManipulator.resetTransparencyOfEntities(entities);
                break;
            case "Pulsation":
                break;
            case "Flashing":
                break;
            case "Rotation":
                break;
        }
        domHelper.resetUI();
    }


    return {
        initialize: initialize,
        activate: activate,
        reset: reset
    }

})();