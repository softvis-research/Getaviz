var metricController = (function () {

    var controllerConfig = {
        metrics: [
            "Number of Statements",
            "Date of Creation",
            "Date of Last Change"
        ],
        mappings: [
            "color"
        ]
    };

    var metric = "Number of Statements";
    var mapping = "color";

    function initialize(setupConfig) {
        application.transferConfigParams(setupConfig, controllerConfig);
    }

    function activate(rootDiv) {
        buildUI(rootDiv);
    }

    function buildUI(rootDiv) {

        let cssLink = document.createElement('link');
         cssLink.type = 'text/css';
         cssLink.rel = 'stylesheet';
         cssLink.href = 'scripts/Metric/metricBox.css';
         document.getElementsByTagName('head')[0].appendChild(cssLink);


        var metricTextNode = document.createElement("p");
        metricTextNode.id = "metrics";
        metricTextNode.innerText = "Metrics";
        rootDiv.appendChild(metricTextNode);

        var metricDropDownDiv = document.createElement("div");
        metricDropDownDiv.id = "metricDropDown";
        rootDiv.appendChild(metricDropDownDiv);

        $("#metricDropDown").jqxDropDownList({ source: controllerConfig.metrics, placeHolder: "Select Metric", width: 250, height: 30 })

        var metricFromTextNode = document.createElement("p");
        metricFromTextNode.id = "metricFromText";
        metricFromTextNode.innerText = "Metric - From";
        rootDiv.appendChild(metricFromTextNode);

        var metricFromInput = document.createElement("input");
        metricFromInput.type = "text";
        metricFromInput.id = "metricFromInput";
        rootDiv.appendChild(metricFromInput);

        $("#metricFromInput").jqxInput({ placeHolder: "From", width: 100, height: 30, minLength: 1 });


        var metricToTextNode = document.createElement("p");
        metricToTextNode.id = "metricToText";
        metricToTextNode.innerText = "Metric - To";
        rootDiv.appendChild(metricToTextNode);

        var metricToInput = document.createElement("input");
        metricToInput.type = "text";
        metricToInput.id = "metricToInput";
        rootDiv.appendChild(metricToInput);

        $("#metricToInput").jqxInput({ placeHolder: "To", width: 100, height: 30, minLength: 1 });

        
        var mappingTextNode = document.createElement("p");
        mappingTextNode.id = "mappings";
        mappingTextNode.innerText = "Mappings";
        rootDiv.appendChild(mappingTextNode);

        var mappingDropDownDiv = document.createElement("div");
        mappingDropDownDiv.id = "mappingDropDown";
        rootDiv.appendChild(mappingDropDownDiv);

        $("#mappingDropDown").jqxDropDownList({ source: controllerConfig.mappings, placeHolder: "Select Mapping", width: 250, height: 30 })
        
        
        var mappingFromTextNode = document.createElement("p");
        mappingFromTextNode.id = "mappingFromText";
        mappingFromTextNode.innerText = "Mapping - From";
        rootDiv.appendChild(mappingFromTextNode);

        var mappingFromInput = document.createElement("input");
        mappingFromInput.type = "text";
        mappingFromInput.id = "mappingFromInput";
        rootDiv.appendChild(mappingFromInput);

        $("#mappingFromInput").jqxInput({ placeHolder: "From", width: 100, height: 30, minLength: 1 });


        var mappingToTextNode = document.createElement("p");
        mappingToTextNode.id = "mappingToText";
        mappingToTextNode.innerText = "Mapping - To";
        rootDiv.appendChild(mappingToTextNode);

        var mappingToInput = document.createElement("input");
        mappingToInput.type = "text";
        mappingToInput.id = "mappingToInput";
        rootDiv.appendChild(mappingToInput);

        $("#mappingToInput").jqxInput({ placeHolder: "To", width: 100, height: 30, minLength: 1 });     
        
        var executeButtonDiv = document.createElement("div");
        executeButtonDiv.id = "executeButton";
        executeButtonDiv.value = "Execute";
        rootDiv.appendChild(executeButtonDiv);

        $("#executeButton").jqxButton({ theme: "metro", height: 20 });
        $("#executeButton").click(executeButtonClicked);

    }

    async function executeButtonClicked(event) {
        var cypherQuery = "MATCH (n) where n.type_name = 'Method' and n.number_of_statements > '5' RETURN n.object_name";

        var response = await getNeo4jData(cypherQuery);

        var entities = [];

        response[0].data.forEach( element => { 
            entities.push(element.row[0]);            
        });

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

    }


    return {
        initialize: initialize,
        activate: activate,
        reset: reset
    }

})();