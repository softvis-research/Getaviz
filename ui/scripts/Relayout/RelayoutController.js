var relayoutController = (function () {

    // list of root packages in the "origin" category which can be re-layouted
    let originModelElements = [];

    let layouter;

    const layoutConfig = {
        buildingHorizontalGap: 3,
    }

    function initialize(setupConfig) {
        application.transferConfigParams(setupConfig, layoutConfig);

        // pull the layouter library into the controller object so we don't have to type the package name
        Object.assign(relayoutController, layout_multiplatform.org.getaviz.generator.city.kotlin);

        layouter = new relayoutController.LightMapLayouter(layoutConfig.buildingHorizontalGap);

        const allEntities = Array.from(model.getAllEntities().values());
        addOriginElements(allEntities);
    }

    function activate(rootDiv) {
        const button = document.createElement('button');
        button.id = "relayout-button";
        button.innerText = "Re-layout visible entities";
        button.addEventListener('click', relayoutAllVisibleOriginElements);
        rootDiv.appendChild(button);

        events.loaded.on.subscribe(onEntitiesLoaded);
    }

    function addOriginElements(newElements) {
        for (const element of newElements) {
            if (element.allParents.length === 0 && element.creator !== "SAP" && element.iteration === 0) {
                originModelElements.push(element);
            }
        }
    }

    function onEntitiesLoaded(applicationEvent) {
        addOriginElements(applicationEvent.entitites);
    }

    function relayoutAllVisibleOriginElements() {
        const nodeArrayList = mapToLayouterNodes(originModelElements);
        layouter.calculateWithVirtualRoot(nodeArrayList);
        transferAttributes(nodeArrayList);
        document.querySelector('a-scene').flushToDOM(true);
    }

    // maps Entity to relayoutController.Node - hidden elements will be skipped
    // returns a Kotlin ArrayList - the library doesn't work with JS arrays internally
    function mapToLayouterNodes(elements) {
        const ArrayList = kotlin.kotlin.collections.ArrayList;

        const jsArray = elements.filter(element => !element.filtered).map(element => {
            // only get size for leaf elements, for all others it will be recalculated anyway
            if (element.children.length > 0) {
                return new relayoutController.Node(element.id, 0, 0, 0, 0, mapToLayouterNodes(element.children));
            } else {
                const domObject = document.getElementById(element.id);
                if (domObject === null) return;
                // cylinders have no width/depth property, so calculate the bounding square and use that
                const radius = Number(domObject.getAttribute("radius"));
                const width = radius ? 2*radius : Number(domObject.getAttribute("width"));
                const depth = radius ? 2*radius : Number(domObject.getAttribute("depth"));

                return new relayoutController.Node(element.id, 0, 0, width, depth, new ArrayList([]));
            }
        });

        return new ArrayList(jsArray);
    }

    // takes a Kotlin ArrayList
    function transferAttributes(elements) {
        elements.toArray().forEach(node => {
            const domObject = document.getElementById(node.id);
            if (domObject === null) return;
            domObject.setAttribute("position", {x: node.centerX, z: node.centerY});
            // don't bother updating dimensions for leaf nodes
            if (node.children.size > 0) {
                // all cylinders are leaf nodes, so we don't need to worry about them
                domObject.setAttribute("width", node.width);
                domObject.setAttribute("depth", node.length);

                transferAttributes(node.children);
            }
        });
    }


    return {
        initialize: initialize,
        activate: activate,

        relayoutAllVisibleOriginElements: relayoutAllVisibleOriginElements,
    }
})();