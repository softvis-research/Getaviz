var featureExplorerController = (function () {

    const featureExplorerTreeID = 'featureExplorerTree';
    const jQFeatureExplorerTree = '#featureExplorerTree';

    let zTreeObject;

    let featureTreePart = [];
    let traceTypeTreePart = [];
    let traceTreePart = [];
    let completeTree = [];

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

        buildTree();
    }

    function buildTree() {
        const classEntities = model.getEntitiesByType('Class');
        const methodEntities = model.getEntitiesByType('Method');
        const entities = classEntities.concat(methodEntities);

        entities.forEach(function (entity) {
            if (entity.featureAffiliations && entity.featureAffiliations.length > 0) {
                entity.featureAffiliations.forEach(function (featureAffiliation) {
                    if (featureAffiliation.feature.includes('not_')) {

                    } else {
                        if (featureAffiliation.feature.includes('_and_')) {
                            let andFeatures = featureAffiliation.feature.split('_and_');
                            andFeatures.forEach(function (andFeature) {
                                if (!featureTreeIncludes(andFeature)) {
                                    addFeatureToFeatureTree(andFeature);
                                }
                                addTraceToFeatureTree(andFeature, featureAffiliation.traceType, entity);
                            });
                        }
                        else {
                            if (!featureTreeIncludes(featureAffiliation.feature)) {
                                addFeatureToFeatureTree(featureAffiliation.feature);
                            }
                            addTraceToFeatureTree(featureAffiliation.feature, featureAffiliation.traceType, entity);
                        }
                    }
                });
            }
        });

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
        setCanvasColors();
        zTreeObject = $.fn.zTree.init($(jQFeatureExplorerTree), settings, completeTree);
    }

    function addFeatureToFeatureTree(featureName) {
        let featureNode = {
            id: featureName,
            open: false,
            checked: true,
            parentId: '',
            name: featureName.charAt(0).toUpperCase() + featureName.slice(1).toLowerCase(),
            feature: featureName,
        }
        featureTreePart.push(featureNode);
        const traceTypes = ['Class', 'Class Refinement', 'Method', 'Method Refinement'];
        traceTypes.forEach(function (traceType) {
            let traceTypeNode = {
                id: (featureName + '_' + traceType).replace(' ', ''),
                open: false,
                checked: true,
                parentId: featureName,
                name: traceType,
                feature: featureName,
            }
            traceTypeTreePart.push(traceTypeNode);
        });
    }

    function addTraceToFeatureTree(featureName, traceType, entity) {
        let node = {
            id: featureName + '_' + traceType.replace(' ', '') + '_' + entity.id,
            open: false,
            checked: true,
            parentId: (featureName + '_' + traceType).replace(' ', ''),
            name: entity.qualifiedName,
            entityId: entity.id,
            feature: featureName,
        }
        traceTreePart.push(node);
    }

    function featureTreeIncludes(featureName) {
        let includes;
        featureTreePart.forEach(function (featureNode) {
            if (featureNode.id == featureName) {
                includes = true;
                return;
            }
        })
        return includes;
    }

    function setIconColors() {
        const numberOfColors = featureTreePart.length;
        const colors = [];
        for(let i = 0; i < featureTreePart.length; ++i) {
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
        featureColorMap.forEach(function (value, key, map) {
            let entities = [];
            traceTreePart.forEach(function (node) {
                if (key == node.feature) {
                    entities.push(model.getEntityById(node.entityId));
                }
            });
            canvasManipulator.changeColorOfEntities(entities, value);
        });
    }
    
    /**
     * Callbacks
     */
    function zTreeOnCheck(event, treeId, treeNode) {
        let nodes = zTreeObject.getChangeCheckedNodes();
        
		let entities = [];
		nodes.forEach(function(node){
            node.checkedOld = node.checked; //fix zTree bug on getChangeCheckedNodes
            if (node.entityId) {
                entities.push(model.getEntityById(node.entityId));
            }
		});
								
		let applicationEvent = {			
			sender: 	featureExplorerController,
			entities:	entities
		};
		
		if (!treeNode.checked){
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