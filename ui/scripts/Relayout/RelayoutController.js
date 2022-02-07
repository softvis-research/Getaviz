var relayoutController = (function () {

    // list of root packages in the "origin" category which can be re-layouted
    let originModelElements = [];

    let layouter;

    const layoutConfig = {
        buildingHorizontalGap: 3,
        emptyDistrictSize: 5,
    }

    function initialize(setupConfig) {
        application.transferConfigParams(setupConfig, layoutConfig);

        // pull the layouter library into the controller object so we don't have to type the package name
        Object.assign(relayoutController, layout_multiplatform.org.getaviz.generator.city.kotlin);

        // internal config object for the kotlin implementation that mostly mirrors layoutConfig
        const internalLayoutConfig = Object.assign(relayoutController.LightMapLayouterConfig_init(), layoutConfig);
        layouter = new relayoutController.LightMapLayouter(internalLayoutConfig);

        const allEntities = Array.from(model.getAllEntities().values());
        addOriginElements(allEntities);
    }

    function activate(rootDiv) {
        rootDiv.innerHTML = `
            <button id="relayout-button">
                Re-layout visible entities
            </button>`;

        document.getElementById("relayout-button").addEventListener('click', relayoutAllVisibleOriginElements);

        events.loaded.on.subscribe(onEntitiesLoaded);
        events.filtered.off.subscribe(onEntitiesUnfiltered);
    }

    function addOriginElements(newElements) {
        for (const element of newElements) {
            if (element.allParents.length === 0 && element.creator !== "SAP" && element.iteration === 0) {
                originModelElements.push(element);
            }
        }
    }

    function onEntitiesLoaded(applicationEvent) {
        addOriginElements(applicationEvent.entities);
        relayoutAllVisibleOriginElements();
    }

    function onEntitiesUnfiltered(applicationEvent) {
        if (document.getElementById("layout-on-unhide").checked) {
            relayoutAllVisibleOriginElements();
        }
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
                return new relayoutController.Node(element.id, element.name, 0, 0, 0, 0, mapToLayouterNodes(element.children), element.isDistrict);
            } else {
                const domObject = document.getElementById(element.id);
                if (domObject === null) return;
                // cylinders have no width/depth property, so calculate the bounding square and use that
                const radius = Number(domObject.getAttribute("radius"));
                const width = radius ? 2*radius : Number(domObject.getAttribute("width"));
                const depth = radius ? 2*radius : Number(domObject.getAttribute("depth"));

                return new relayoutController.Node(element.id, element.name, 0, 0, width, depth, new ArrayList([]), element.isDistrict);
            }
        });

        return new ArrayList(jsArray);
    }

    // takes a Kotlin ArrayList
    function transferAttributes(elements) {
        elements.toArray().forEach(node => {
            const domObject = document.getElementById(node.id);
            if (domObject === null) return;

            // manually re-set the y value - leaving it out should preserve the old value, but this only works the first time
            // if you try to set the *same* position a *second* time, THEN it suddenly gets clobbered to 0 if you leave it out
            domObject.setAttribute("position", {
                x: node.centerX,
                y: domObject.getAttribute("position").y,
                z: node.centerY
            });

            // non-district elements won't be resized
            if (node.isDistrict) {
                // Usually, updating the HTML attributes updates the AFrame geometry component automatically -
                // except sometimes it doesn't, so update it manually as well.
                // But we still need to set the HTML attributes because that's what we'll be reading from down the line
                domObject.setAttribute("width", node.width);
                domObject.setAttribute("depth", node.length);

                domObject.updateComponent("geometry", {width: node.width, depth: node.length}, false);

                if (node.children.size > 0) {
                    transferAttributes(node.children);
                }
            }
        });
    }


    return {
        initialize: initialize,
        activate: activate,

        relayoutAllVisibleOriginElements: relayoutAllVisibleOriginElements,
    }
})();