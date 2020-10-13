var featureExplorerController = (function () {

    /**
     * General data
     */
    let traces = []; // all entities that are traces 
    let features = []; // all found features

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

    function initialize(controllerSetup) {

    }

    function activate(rootDiv) {
        let zTreeDiv = document.createElement('div');
        zTreeDiv.id = 'zTreeDiv';

        let featureExplorerTreeUl = document.createElement('ul');
        featureExplorerTreeUl.id = featureExplorerTreeID;
        featureExplorerTreeUl.setAttribute('class', 'ztree');

        zTreeDiv.appendChild(featureExplorerTreeUl);
        rootDiv.appendChild(zTreeDiv);

        filterTracesFromEntities();
        setMainAffiliationForEachTrace();
        buildTree();
        setCanvasColors();
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
        setIconColors();
        zTreeObject = $.fn.zTree.init($(jQFeatureExplorerTree), settings, completeTree);
    }

    function buildFeatureTreePart() {
        features.forEach(function (feature) {
            let featureNode = {
                id: feature,
                open: false,
                checked: true,
                parentId: '',
                name: feature.charAt(0).toUpperCase() + feature.slice(1).toLowerCase(),
                feature: feature,
            }
            featureTreePart.push(featureNode);
        })
    }

    function buildTraceTypeTreePart() {
        const traceTypes = ['Class', 'Method', 'Class Refinement', 'Method Refinement'];
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

    function setIconColors() {
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

        completeTree.forEach(function (node) {
            const color = featureColorMap.get(node.feature);
            node.icon = getColoredIcon(color);
            node.iconSkin = "zt";
        })
    }

    function getColoredIcon(color) {
        const iconString = "<svg xmlns='http://www.w3.org/2000/svg' width='20' height='20'><circle cx='10' cy='10' r='10' fill='" + color + "' /></svg>";
        const base64Icon = window.btoa(iconString);
        return '"data:image/svg+xml;base64,' + base64Icon + '"';
    }

    function setCanvasColors() {
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
        traces.forEach(function (trace) {
            switch (trace.elementarySetMain) {
                case "And":
                case "Or":
                    // 1. Find colors to Features 2. Build Canvas with colors (look at aframe-test for algorithm inspiration) 3. Set Canvas as material
                    break;
                default:
                    break;
            }
        })
    }

    /**
     * Callbacks
     */
    function zTreeOnCheck(event, treeId, treeNode) {
        let nodes = zTreeObject.getChangeCheckedNodes();

        let entities = [];
        nodes.forEach(function (node) {
            node.checkedOld = node.checked; //fix zTree bug on getChangeCheckedNodes
            if (node.entityId) {
                entities.push(model.getEntityById(node.entityId));
            }
        });

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

    function zTreeOnClick(treeEvent, treeId, treeNode) {
        if (treeNode.entityId) {
            canvasManipulator.flyToEntity(model.getEntityById(treeNode.entityId));
        }
    }



    return {
        initialize: initialize,
        activate: activate
    };
})();