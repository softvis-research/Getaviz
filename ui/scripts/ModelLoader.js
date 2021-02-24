let neo4jModelLoadController = (function () {

    //  Default config (Fallback in case it's a new setup without a proper config)
    let controllerConfig = {
        url: 'http://localhost:7474/db/data/transaction/commit',
        loadStartData: 'rootPackages',
        showLoadSpinner: true
    };

    function initialize() {
        // Override config with ones from setup
        if (setup.neo4jModelLoadConfig) {
            controllerConfig = {...controllerConfig, ...setup.neo4jModelLoadConfig}
        }
    };


    // Get metadata on launch
    async function loadStartMetaData() {
        const cypherQuery = `MATCH (p:ACityRep) RETURN p`;
        const nodeMetadata = await getMetadataForQuery(cypherQuery);
        model.initialize(nodeMetadata);
    }


    // Returning parent nodes metadata, prepared to be inserted into model.js
    async function getMetadataForQuery(cypherQuery) {
        let response = await getNeo4jData(cypherQuery);
        let data = await getMetadataFromResponse(response);
        return data;
    }


    // Return metadata object from response
    async function getMetadataFromResponse(response) {
        if (!response[0].data) {
            return [];
        }

        return response[0].data.map((obj) => {
            return JSON.parse(obj.row[0].metaData);
        })
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
            let response = await fetch(controllerConfig.url, {
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


    return {
        initialize: initialize,
        loadStartMetaData: loadStartMetaData,
    };
})();