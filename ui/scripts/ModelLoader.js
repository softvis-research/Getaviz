let neo4jModelLoadController = (function () {

    // default config (fallback in case it's a new setup without a proper config)
    let controllerConfig = {
        url: 'http://localhost:7474/db/data/transaction/commit',
        loadStartData: 'rootPackages',
        showLoadSpinner: true
    };

    // these counters are specific to a loading process, not an overall count
    let loadedElements = 0;
    let totalElements = 0;
    let loaderElement = null;
    let loaderDataElement = null;
    let keepLoaderAlive = false;

    function initialize() {
        // Override config with ones from setup
        if (setup.neo4jModelLoadConfig) {
            controllerConfig = {...controllerConfig, ...setup.neo4jModelLoadConfig}
        }
        if (controllerConfig.showLoadSpinner) {
            createLoadSpinner();
        }
    };

    function createLoadSpinner() {
        const loader = document.createElement('div');
        loader.id = 'load-spinner-bg';
        loader.className = 'hidden';
        loader.innerHTML = '<div class="load-spinner-body"><div class="loader">Loading...</div><div id="load-spinner-data">Loading elements...</div>';
        document.body.appendChild(loader);

        loaderElement = document.getElementById('load-spinner-bg');
        loaderDataElement = document.getElementById('load-spinner-data');
    }

    function showLoadSpinner() {
        if (loaderElement) loaderElement.classList.remove('hidden');
    }

    function hideLoadSpinner() {
        if (loaderElement) loaderElement.classList.add('hidden');
    }

    function updateLoadSpinner(additionalLoadedElements, additionalTotalElements, resetKeepAlive = false) {
        if (!controllerConfig.showLoadSpinner) return;

        if (resetKeepAlive) keepLoaderAlive = false;

        loadedElements += additionalLoadedElements || 0;
        totalElements += additionalTotalElements || 0;
        if (loadedElements === totalElements) {
            if (!keepLoaderAlive) {
                hideLoadSpinner();
                loadedElements = 0;
                totalElements = 0;
            }
        } else {
            loaderDataElement.innerHTML = `Loading elements: ${loadedElements} of ${totalElements}`;
            showLoadSpinner();
        }
    }

    // load all model data and metadata that is necessary at launch
    async function loadInitialData() {
        showLoadSpinner();
        const data = await queryRootNodes();
        updateLoadSpinner(0, data[0].data.length);
        const createdEntities = await addNodesAsHidden(data, false);
        updateLoadSpinner(createdEntities.length, 0);
        events.loaded.on.publish({
            entities: createdEntities,
            hidden: true
        });
    }

    // load all nodes that are (direct or indirect) children of or contained by the given root
    async function loadAllChildrenOf(entityId, loadAsHidden) {
        showLoadSpinner();
        const nodeData = await queryAllChildrenOf(entityId);
        updateLoadSpinner(0, nodeData[0].data.length);
        let createdEntities;
        if (loadAsHidden) {
            createdEntities = await addNodesAsHidden(nodeData, true);
        } else {
            createdEntities = await addNodes(nodeData);
        }

        const parentEntity = model.getEntityById(entityId);
        parentEntity.hasUnloadedChildren = false;

        updateLoadSpinner(createdEntities.length, 0);
        events.log.info.publish({ text: `loaded all ${createdEntities.length} children of entity ${entityId}` });

        events.loaded.on.publish({
            entities: createdEntities,
            hidden: loadAsHidden,
            parentId: entityId
        });
    }

    async function loadTreesContainingAnyOf(entityIds) {
        // ensure the loader element does not get cleaned prematurely
        keepLoaderAlive = true;
        showLoadSpinner();
        const rootNodeQueries = entityIds.map(id => queryRelatedRootNodeIdOf(id));
        const rootNodeIdSet = new Set();
        await Promise.all(rootNodeQueries)
            .then(responses => responses.map(getSinglePropertyFromResponse))
            .then(ids => ids.forEach(id => rootNodeIdSet.add(id)));

        const childrenQueries = [...rootNodeIdSet.values()].map(id => loadAllChildrenOf(id, true));
        //childrenQueries.push(new Promise((resolve) => setTimeout(resolve, 5000)));
        return Promise.all(childrenQueries).then(() => updateLoadSpinner(0, 0, true));
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
        if (!response || !response[0].data) {
            return [];
        }

        return response[0].data.map((obj) => {
            return JSON.parse(obj.row[0].metaData);
        });
    }

    // get array of each element's parsed AFrame properties
    async function getAframeDataFromResponse(response) {
        if (!response || !response[0].data) {
            return [];
        }

        return response[0].data.map((obj) => {
            return JSON.parse(obj.row[0].aframeProperty);
        });
    }

    function getSinglePropertyFromResponse(response) {
        if (!response || !response[0].data || response[0].data.length === 0) {
            return null;
        }
        return response[0].data[0].row[0];
    }

    async function queryRootNodes() {
        return await getNeo4jData(`MATCH (n:ACityRep) WHERE NOT ()-[:CHILD]->(n) RETURN n`);
    }

    async function queryAllChildrenOf(entityId) {
        return await getNeo4jData(`MATCH ({hash: "${entityId}"})-[:CHILD|CONTAINS*1..]->(n:ACityRep) RETURN n`);
    }

    async function queryRelatedRootNodeIdOf(entityId) {
        return await getNeo4jData(`MATCH (n:ACityRep) WHERE NOT ()-[:CHILD|CONTAINS]->(n) AND (n)-[:CHILD|CONTAINS*0..]->({hash: "${entityId}"}) RETURN n.hash`);
    }

    // Universal method to load a data from Neo4j using imported cypher-query
    async function getNeo4jData(cypherQuery) {
        const payload = {
            'statements': [
                // neo4j requires keyword "statement", so leave as is
                { 'statement': cypherQuery }
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
            events.log.error.publish({ text: "Failed to access database: " + error });
        }
    }


    return {
        initialize: initialize,
        loadInitialData: loadInitialData,
        loadAllChildrenOf: loadAllChildrenOf,
        loadTreesContainingAnyOf: loadTreesContainingAnyOf
    };
})();
