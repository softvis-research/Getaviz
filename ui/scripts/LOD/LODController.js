var lodController = (function() {
    
    var MOUSE_BUTTON_LEFT = 1;
    var MOUSE_BUTTON_RIGHT = 2;

    var rootLODElements;

    var activated = false;
    
    var controllerConfig = {};
    
    function initialize(setupConfig) {
        application.transferConfigParams(setupConfig, controllerConfig);
    }
    
    function activate() {
        // 1) Load 3D info into AFrame-Canvas
        $.get(modelUrl + "/lod/lodobjects.html", content => $("#aframe-canvas").append(content));
        
        // 2) Load relation info into Model using model.createEntity
        $.getJSON(modelUrl + "/lod/lodinfo.json", expandModelWithLODInfo).done(() => {
            // 3) Hide everything except LOD root
            canvasManipulator.hideEntities(model.getAllEntities());
            canvasManipulator.showEntities(rootLODElements);
            // Keep clouds visible
            canvasManipulator.showEntities(model.getEntitiesByType("Reference").filter(entity => entity.name == "Cloud"));
        });

        // 4) Subscribe to Hover-Events
        actionController.actions.mouse.key[MOUSE_BUTTON_LEFT].down.subscribe(onLeftClick);
		actionController.actions.mouse.key[MOUSE_BUTTON_RIGHT].down.subscribe(onRightClick);
        
        activated = true;
    }
    
    function deactivate() {
        reset();
        activated = false;
    }
    
    function reset() {
        let toHide = [], toShow = [];
        model.getAllEntities().forEach(entity => {
            // Hide all LOD objects & show all other entities
            if (entity.type === "LODObject") {
                toHide.push(entity)
            } else {
                toShow.push(entity);
            }
            // Clear wrapper info
            if (entity.replacedBy) {
                entity.replacedBy = undefined;
            }
        });
        canvasManipulator.hideEntities(toHide);
        canvasManipulator.showEntities(toShow);
    }
    
    function expandModelWithLODInfo(lodinfo) {
        let potentialRootElements = new Set();
        // First pass, create all entities
        lodinfo.blocks.forEach(block => {
            let entity = model.createEntity("LODObject", block.id, null, null, null);
            potentialRootElements.add(entity);
            entity.replaces = [];
        });
        // Second pass, add references to replaced entities
        lodinfo.blocks.forEach(block => {
            let entity = model.getEntityById(block.id);
            block.replaces.forEach(replacedEntityID => {
                let replacedEntity = model.getEntityById(replacedEntityID);
                entity.replaces.push(replacedEntity);
                // Eliminate all child elements
                potentialRootElements.delete(replacedEntity);
            });
        });
        // Root LOD objects
        rootLODElements = potentialRootElements;
    }
    
    function onLeftClick(applicationEvent) {
        let entity = applicationEvent.entity;
        if (entity?.type === "LODObject") {
            unHover(entity) // Unhover LOD wrapper
            canvasManipulator.hideEntities([entity]);   // Hide LOD wrapper
            canvasManipulator.showEntities(entity.replaces);    // Show replaced contents
            entity.replaces.forEach(e => e.replacedBy = entity);  // Keep track of hidden wrapper
        }
    }
    
    function onRightClick(applicationEvent) {
        let entity = applicationEvent.entity;
        let lodParent = entity?.replacedBy;
        if (lodParent) {
            unHover(entity);
            canvasManipulator.showEntities([lodParent]);
            hideChildren(lodParent);
        }
    }

    function unHover(entity) {
        var applicationEvent = {
            sender: lodController,
            entities: [entity]
        };
        events.hovered.off.publish(applicationEvent);
    }

    function hideChildren(element) {
        let children = element.replaces;
        if (children) {
            canvasManipulator.hideEntities(children);
            children.forEach(child => hideChildren(child));
        }
    }
    
    return {
        initialize: initialize,
        activate: activate,
        deactivate: deactivate,
        reset: reset
    };
    
})();
