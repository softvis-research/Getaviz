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


    // load all model data and metadata that is necessary at launch
    async function loadInitialData() {
        const data = await queryRootNodes();
        const createdEntities = await addNodesAsHidden(data, false);
        events.loaded.on.publish({
            entities: createdEntities,
            hidden: true
        });
    }

    // load all nodes that are (direct or indirect) children of or contained by the given root
    async function loadAllChildrenOf(entityId, loadAsHidden) {
        const nodeData = await queryAllChildrenOf(entityId);
        let createdEntities;
        if (loadAsHidden) {
            createdEntities = await addNodesAsHidden(nodeData, true);
        } else {
            createdEntities = await addNodes(nodeData);
        }

        const parentEntity = model.getEntityById(entityId);
        parentEntity.hasUnloadedChildren = false;

        events.loaded.on.publish({
            entities: createdEntities,
            hidden: loadAsHidden,
            parentId: entityId
        });
    }

    async function addNodesAsHidden(nodeData, areChildrenLoaded) {
        // these can run in parallel and be awaited at the end
        const metadataDone = getMetadataFromResponse(nodeData)
            .then(data => model.createEntititesFromMetadata(data, areChildrenLoaded))
            .then(entities => {
                entities.forEach(entity => { entity.filtered = true; });
                return entities;
            });
        const aframeDataDone = getAframeDataFromResponse(nodeData)
            .then(canvasManipulator.loadAsHiddenFromAframeData);

        await Promise.all([metadataDone, aframeDataDone]);
        return metadataDone;
    }

    async function addNodes(nodeData) {
        // these can run in parallel and be awaited at the end
        const metadataDone = getMetadataFromResponse(nodeData)
            .then(model.createEntititesFromMetadata);
        const aframeDataDone = getAframeDataFromResponse(nodeData)
            .then(canvasManipulator.addElementsFromAframeData);

        await Promise.all([metadataDone, aframeDataDone]);
        return metadataDone;
    }

    // get array of each element's parsed metadata
    async function getMetadataFromResponse(response) {
        if (!response[0].data) {
            return [];
        }

        return response[0].data.map((obj) => {
            return JSON.parse(obj.row[0].metaData);
        });
    }

    // get array of each element's parsed AFrame properties
    async function getAframeDataFromResponse(response) {
        if (!response[0].data) {
            return [];
        }

        return response[0].data.map((obj) => {
            return JSON.parse(obj.row[0].aframeProperty);
        });
    }

    async function queryRootNodes() {
        return await getNeo4jData(`MATCH (n:ACityRep) WHERE NOT ()-[:CHILD]->(n) RETURN n`);
    }

    async function queryAllChildrenOf(entityId) {
        return await getNeo4jData(`MATCH ({hash: "${entityId}"})-[:CHILD|CONTAINS*1..]->(n:ACityRep) RETURN n`);
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
        loadInitialData: loadInitialData,
        loadAllChildrenOf: loadAllChildrenOf,
    };
})();