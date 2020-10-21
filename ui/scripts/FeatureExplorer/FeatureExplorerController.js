var featureExplorerController = (function () {

    /**
     * General data
     */
    let traces = []; // all entities that are traces 
    let features = []; // all found features
    let traceTypes = ["Class", "Class Refinement", "Method", "Method Refinement"];

    /**
     * zTree data
     */
    const featureExplorerTreeID = 'featureExplorerTree';
    const jQFeatureExplorerTree = '#featureExplorerTree';

    let zTreeObject;

    let featureTreePart = [];
    let traceTypeTreePart = [];
    let traceTreePart = [];
    let completeTree = [];

    /**
     * Color data
     */
    let featureColorMap = new Map();
    const clearedColor = "#353559"

    function initialize(controllerSetup) {

    }

    function activate(rootDiv) {
        buildControls(rootDiv);
        addControlEvents();

        let zTreeDiv = document.createElement('div');
        zTreeDiv.id = 'featureExplorerZTreeDiv';

        let featureExplorerTreeUl = document.createElement('ul');
        featureExplorerTreeUl.id = featureExplorerTreeID;
        featureExplorerTreeUl.setAttribute('class', 'ztree');

        zTreeDiv.appendChild(featureExplorerTreeUl);
        rootDiv.appendChild(zTreeDiv);

        filterTracesFromEntities();
        setMainAffiliationForEachTrace();
        buildTree();
        setDefaultColors();
        buildColorPicker();
        addTreeEvents();
    }

    function buildControls(rootDiv) {
        let controlsDiv = document.createElement('div');
        controlsDiv.id = 'featureControls';
        controlsDiv.style = "margin-top: 6px";

        let clearColorsButton = document.createElement('button');
        clearColorsButton.id = "clearColors";
        clearColorsButton.appendChild(document.createTextNode("Clear Colors"));
        controlsDiv.appendChild(clearColorsButton);

        let resetColorsButton = document.createElement('button');
        resetColorsButton.id = "resetColors";
        resetColorsButton.appendChild(document.createTextNode("Reset Colors"));
        controlsDiv.appendChild(resetColorsButton);

        rootDiv.appendChild(controlsDiv);

        $("#clearColors").jqxButton({
            width: 150,
            height: 25,
            theme: "metro",
        });

        $("#resetColors").jqxButton({
            width: 150,
            height: 25,
            theme: "metro",
        });
    }

    function addControlEvents() {
        $("#clearColors").on('click', function () {
            featureColorMap.forEach(function (value, key) {
                featureColorMap.set(key, clearedColor);
            })
            updateColors();
        })
        $("#resetColors").on('click', function () {
            setDefaultColors();
        })
    }

    function filterTracesFromEntities() {
        const classEntities = model.getEntitiesByType('Class');
        const methodEntities = model.getEntitiesByType('Method');
        const entities = classEntities.concat(methodEntities);

        entities.forEach(function (entity) {
            if (entity.featureAffiliations && entity.featureAffiliations.length > 0) {
                traces.push(entity);

                entity.featureAffiliations.forEach(function (featureAffiliation) {
                    if (featureAffiliation.feature.includes('not_')) {
                        return;
                    }
                    if (featureAffiliation.feature.includes('_and_')) {
                        let andFeatures = featureAffiliation.feature.split('_and_');
                        andFeatures.forEach(function (feature) {
                            if (!features.includes(feature)) {
                                features.push(feature);
                            }
                        })
                    }
                    else {
                        if (!features.includes(featureAffiliation.feature)) {
                            features.push(featureAffiliation.feature);
                        }
                    }
                })
            }
        })
    }

    function setMainAffiliationForEachTrace() {
        traces.forEach(function (trace) {
            let isSet;
            trace.featureAffiliations.forEach(function (affiliation) {
                if (!isSet) {
                    if (affiliation.isRefinement) {
                        return;
                    }
                    else {
                        trace.elementarySetMain = affiliation.elementarySet;
                        if (affiliation.elementarySet == "And") {
                            let andFeatures = affiliation.feature.split('_and_');
                            trace.featuresMain = andFeatures;
                        } else {
                            trace.featuresMain = [affiliation.feature]
                        }
                        isSet = true;
                    }
                } else {
                    if (!affiliation.isRefinement && trace.elementarySetMain == affiliation.elementarySet) {
                        trace.featuresMain.push(affiliation.feature);
                    }
                }
            })
        })
    }

    function buildTree() {
        buildFeatureTreePart();
        buildTraceTypeTreePart();
        buildTraceTreePart();

        let settings = {
            check: {
                enable: true,
                chkboxType: { 'Y': 'ps', 'N': 's' }
            },
            data: {
                simpleData: {
                    enable: true,
                    idKey: 'id',
                    pIdKey: 'parentId',
                    rootPId: ''
                }
            },
            callback: {
                onCheck: zTreeOnCheck,
                onClick: zTreeOnClick,
            },
            view: {
                showLine: false,
                showIcon: true,
                selectMulti: false
            }
        };

        completeTree = featureTreePart.concat(traceTypeTreePart.concat(traceTreePart));
        zTreeObject = $.fn.zTree.init($(jQFeatureExplorerTree), settings, completeTree);
    }

    function buildFeatureTreePart() {
        features.forEach(function (feature) {
            let featureNode = {
                id: feature,
                open: false,
                checked: true,
                parentIdfeature,
                feature: feature,
            }
            featureTreePart.push(featureNode);
        })
    }

    function buildTraceTypeTreePart() {
        features.forEach(function (feature) {
            traceTypes.forEach(function (traceType) {
                let traceTypeNode = {
                    id: (feature + '_' + traceType).replace(' ', ''),
                    open: false,
                    checked: true,
                    parentId: feature,
                    name: traceType,
                    feature: feature,
                    nocheck: traceType == "Class Refinement" || traceType == "Method Refinement"
                }
                traceTypeTreePart.push(traceTypeNode);
            })
        })
    }

    function buildTraceTreePart() {
        traces.forEach(function (trace) {
            trace.featureAffiliations.forEach(function (featureAffiliation) {
                if (featureAffiliation.feature.includes('not_')) {

                } else {
                    if (featureAffiliation.feature.includes('_and_')) {
                        let andFeatures = featureAffiliation.feature.split('_and_');
                        andFeatures.forEach(function (andFeature) {
                            addTraceToFeatureTree(andFeature, featureAffiliation, trace);
                        });
                    }
                    else {
                        addTraceToFeatureTree(featureAffiliation.feature, featureAffiliation, trace);
                    }
                }
            });
        });
    }

    function addTraceToFeatureTree(featureName, featureAffiliation, entity) {
        let node = {
            id: featureName + '_' + featureAffiliation.traceType.replace(' ', '') + '_' + entity.id,
            open: false,
            checked: true,
            parentId: (featureName + '_' + featureAffiliation.traceType).replace(' ', ''),
            name: entity.qualifiedName,
            entityId: entity.id,
            feature: featureName,
            isRefinement: featureAffiliation.isRefinement,
            nocheck: featureAffiliation.isRefinement
        }
        traceTreePart.push(node);
    }

    /**
     * Color functions
     */

    function setDefaultColors() {
        calculateColors();
        updateIconColors();
        updateCanvasColors();
    }

    function updateColors() {
        updateIconColors();
        updateCanvasColors();
    }

    function calculateColors() {
        const numberOfColors = featureTreePart.length;
        const colors = [];
        for (let i = 0; i < featureTreePart.length; ++i) {
            const hue = i * (360 / featureTreePart.length);
            const hslColor = 'hsl(' + hue + ',100%,70%)';
            colors.push(hslColor);
        }

        featureTreePart.forEach(function (featureNode) {
            featureColorMap.set(featureNode.feature, colors.pop());
        })
    }

    function updateIconColors() {
        let nodes = zTreeObject.getNodesByFilter(function () { return true; });
        nodes.forEach(function (node) {
            const color = featureColorMap.get(node.feature);
            node.icon = getColoredIcon(color);
            node.iconSkin = "zt";
            zTreeObject.updateNode(node);
        })
    }

    function getColoredIcon(color) {
        const iconString = "<svg xmlns='http://www.w3.org/2000/svg' width='20' height='20'><circle cx='10' cy='10' r='10' fill='" + color + "' /></svg>";
        const base64Icon = window.btoa(iconString);
        return '"data:image/svg+xml;base64,' + base64Icon + '"';
    }

    function updateCanvasColors() {
        setMonochromeColors();
        setPolychromaticColors();
    }

    function setMonochromeColors() {
        featureColorMap.forEach(function (value, key, map) {
            let entities = [];
            traces.forEach(function (trace) {
                if (trace.elementarySetMain == "Pure" && key == trace.featuresMain[0]) {
                    entities.push(model.getEntityById(trace.id));
                }
            });
            canvasManipulator.changeColorOfEntities(entities, value);
        });
    }

    function setPolychromaticColors() {
        $(".visualizationCanvas").remove(); 
        traces.forEach(function (trace) {
            if (trace.elementarySetMain == "And" || trace.elementarySetMain == "Or") {
                let colors = [];
                trace.featuresMain.forEach(function (feature) {
                    colors.push(featureColorMap.get(feature));
                })

                if (!document.getElementById(getCanvasID(trace.featuresMain))) {
                    buildCanvas(trace.featuresMain, colors);
                }

                canvasManipulator.setMaterialOfEntities([trace], 'src: #' + getCanvasID(trace.featuresMain));
            }
        })
    }

    function buildCanvas(features, colors) {
        let size = 1000;

        let canv = document.createElement("canvas");
        canv.id = getCanvasID(features);
        canv.classList.add('visualizationCanvas');
        canv.width = size;
        canv.height = size;
        let cont = canv.getContext("2d");

        let amountColors = colors.length;
        let stepSize = size / amountColors;
        for (let i = 0; i < amountColors; ++i) {
            cont.beginPath();
            cont.rect(0, i * stepSize, size, stepSize);
            cont.fillStyle = colors[i];
            cont.fill();
        }

        if (document.getElementById("assets") == undefined) {
            let assetElement = document.createElement("a-assets");
            assetElement.id = "assets";
            document.getElementById("aframe-canvas").appendChild(assetElement);
        }

        document.getElementById("assets").appendChild(canv);
    }

    function getCanvasID(features) {
        let id = "";
        features.forEach(function (feature) {
            id += feature + "_";
        });
        id = id.slice(0, -1);
        return id;
    }

    function getAllAffectedNodes(treeNode) {
        let childNodes = [];
        if (treeNode.children) {
            treeNode.children.forEach(function (child) {
                if (!child.nocheck) {
                    childNodes = childNodes.concat(getAllAffectedNodes(child));
                }
            })
        }
        childNodes.push(treeNode);
        return childNodes;
    }

    function addTreeEvents() {
        $("#featureExplorerTree .zt_ico_close").on('click', function (params) {
            let nodeId = this.id.replace("_ico", "");
            let node = zTreeObject.getNodeByTId(nodeId);
            showColorPicker(node);
        })
    }

    function buildColorPicker() {
        let div = document.createElement('div');
        div.id = "colorPickerWindow";
        let header = document.createElement('div');
        let colorPicker = document.createElement('div');
        colorPicker.id = "colorPicker";
        div.appendChild(header);
        div.appendChild(colorPicker);
        document.getElementById("featureExplorerZTreeDiv").appendChild(div);

        $("#colorPickerWindow").jqxWindow({ 
			theme: "metro", 
			isModal: true, 
			autoOpen: false,   
            resizable: false,
            height: 270,
            width: 230,
        });
        $("#colorPickerWindow").on("close", function (event) {
            //alert(this.getAttribute("data-feature") + " is now of color: " + $("#colorPicker").jqxColorPicker("getColor"));
            let feature = this.getAttribute("data-feature");
            let color = "#" + $("#colorPicker").jqxColorPicker("getColor").hex;
            featureColorMap.set(feature, color);
            updateColors();
        })

        $("#colorPicker").jqxColorPicker({ width: 200, height: 200, theme: "metro" });
    }

    function showColorPicker(node) {
        let currentColor = featureColorMap.get(node.id);
        $("#colorPicker").jqxColorPicker("setColor", currentColor);

        $("#colorPickerWindow").attr("data-feature", node.id);
        $("#colorPickerWindow").jqxWindow("setTitle", node.name);
        $("#colorPickerWindow").jqxWindow("open");
    }

    function getElementarySetMain(id) {
        let trace = getTraceById(id);
        if (trace) {
            return trace.elementarySetMain;
        }
    }

    function getFeaturesMain(id) {
        let trace = getTraceById(id);
        if (trace) {
            return trace.featuresMain;
        }
    }

    function getTraceById(id) {
        let result;
        traces.forEach(function (trace) {
            if (trace.id == id) {
                result = trace;
            }
        })
        return result;
    }

    /**
     * Callbacks
     */
    function zTreeOnCheck(event, treeId, treeNode) {
        let nodes = getAllAffectedNodes(treeNode);

        let entities = [];
        nodes.forEach(function (node) {
            if (node.entityId) {
                entities.push(model.getEntityById(node.entityId));
            }
        });

        handleOtherEntries(treeNode, entities);

        let applicationEvent = {
            sender: featureExplorerController,
            entities: entities
        };

        if (!treeNode.checked) {
            events.filtered.on.publish(applicationEvent);
        } else {
            events.filtered.off.publish(applicationEvent);
        }
    }

    function handleOtherEntries(clickedNode, entities) {
        if (features.includes(clickedNode.id) || traceTypes.includes(clickedNode.name)) { // indirect selection
            entities.slice().reverse().forEach(function (entity) { // iterate reverse with shallow copy, otherwise elements are left out
                let otherNodes = zTreeObject.getNodesByFilter(function (node) {
                    return node.entityId == entity.id;
                })
                if (entity.elementarySetMain == "And") {
                    if (clickedNode.checked) { // selected
                        let allFeaturesSelected = true;
                        entity.featuresMain.forEach(function (feature) {
                            let featureNode = zTreeObject.getNodeByParam("id", feature);
                            if (!featureNode.checked) {
                                allFeaturesSelected = false;
                            }
                        })
                        if (!allFeaturesSelected) {
                            entities.splice(entities.indexOf(entity), 1);
                        } else {
                            otherNodes.forEach(function (otherNode) {
                                zTreeObject.checkNode(otherNode, true);
                            })
                        }
                    } else { // deselected
                        otherNodes.forEach(function (otherNode) {
                            zTreeObject.checkNode(otherNode, false);
                        })
                    }
                }
                if (entity.elementarySetMain == "Or") {
                    if (clickedNode.checked) { // selected
                        otherNodes.forEach(function (otherNode) {
                            zTreeObject.checkNode(otherNode, true);
                        })
                    } else { // deselected
                        let allFeaturesDeselected = true;
                        entity.featuresMain.forEach(function (feature) {
                            let featureNode = zTreeObject.getNodeByParam("id", feature);
                            if (featureNode.checked) {
                                allFeaturesDeselected = false;
                            }
                        })
                        if (!allFeaturesDeselected) {
                            entities.splice(entities.indexOf(entity), 1);
                        } else {
                            otherNodes.forEach(function (otherNode) {
                                zTreeObject.checkNode(otherNode, true);
                            })
                        }
                    }
                }
            })
        } else { // direct selection
            entities.forEach(function (entity) {
                // todo
            })
        }
    }

    function zTreeOnClick(treeEvent, treeId, treeNode) {
        if (treeNode.entityId) {
            canvasManipulator.flyToEntity(model.getEntityById(treeNode.entityId));
        }
    }



    return {
        initialize: initialize,
        activate: activate,

        getElementarySetMain: getElementarySetMain,
        getFeaturesMain: getFeaturesMain,
    };
})();