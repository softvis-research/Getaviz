var featureExplorerController = (function () {

    const featureExplorerTreeID = 'featureExplorerTree';
    const jQFeatureExplorerTree = '#featureExplorerTree';

    let zTreeObject;

    let featureTree = [];

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
            view: {
                showLine: false,
                showIcon: false,
                selectMulti: false
            }
        };

        zTreeObject = $.fn.zTree.init($(jQFeatureExplorerTree), settings, featureTree);
    }

    function addFeatureToFeatureTree(featureName) {
        let featureNode = {
            id: featureName,
            open: false,
            checked: true,
            parentId: '',
            name: featureName.charAt(0).toUpperCase() + featureName.slice(1).toLowerCase(),
        }
        featureTree.push(featureNode);
        const traceTypes = ['Class', 'Class Refinement', 'Method', 'Method Refinement'];
        traceTypes.forEach(function (traceType) {
            let traceTypeNode = {
                id: (featureName + '_' + traceType).replace(' ', ''),
                open: false,
                checked: true,
                parentId: featureName,
                name: traceType,
            }
            featureTree.push(traceTypeNode);
        });
    }

    function addTraceToFeatureTree(featureName, traceType, entity) {
        let node = {
            id: featureName + '_' + traceType.replace(' ', '') + '_' + entity.id,
            open: false,
            checked: true,
            parentId: (featureName + '_' + traceType).replace(' ', ''),
            name: entity.qualifiedName,
        }
        featureTree.push(node);
    }

    function featureTreeIncludes(featureName) {
        let includes;
        featureTree.forEach(function (featureNode) {
            if (featureNode.id == featureName) {
                includes = true;
                return;
            }
        })
        return includes;
    }


    return {
        initialize: initialize,
        activate: activate
    };
})();