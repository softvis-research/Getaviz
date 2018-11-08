var canvasManipulator = (function () {

    var colors = {
        darkred: "darkred",
        black: "black",
        orange: "orange",
        darkorange: "darkorange"
    }

    var scene = {};
    var threeJSScene = {};

    var camera;
    var initialCameraView;

    function initialize() {

        scene = document.getElementById(canvasId);
        threeJSScene = scene.object3D;
        camera = document.getElementById("camera");

    }

    function reset() {

    }

    //  working - save old transparency in case it is not 0?
    function changeTransparencyOfEntities(entities, value) {
        entities.forEach(function (entity) {
            let component = document.getElementById(entity.id);
            if (component == undefined) {
                events.log.error.publish({text: "CanvasManipualtor - changeTransparencyOfEntities - components for entityIds not found"});
                return;
            }
            setTransparency(component, value);
        });
    }

    //  working
    function resetTransparencyOfEntities(entities) {
        entities.forEach(function (entity) {
            let component = document.getElementById(entity.id);
            if (component == undefined) {
                events.log.error.publish({text: "CanvasManipualtor - resetTransparencyOfEntities - components for entityIds not found"});
                return;
            }
            setTransparency(component, 1);
        });
    }


    //	working
    function changeColorOfEntities(entities, color) {
        entities.forEach(function (entity) {
            //	in x3dom this function would get entities of the model to change the color of the related object
            //	for reference in canvasHoverController.js: var entity = model.getEntityById(multipartEvent.partID);
            //	this entity gets handed over to the ActionController.js as part of an ApplicationEvent
            if (!(entity == undefined)) {
                var component = document.getElementById(entity.id);
            }
            if (component == undefined) {
                events.log.error.publish({text: "CanvasManipualtor - changeColorOfEntities - components for entityIds not found"});
                return;
            }
            setColor(component, color);
        });
    }

    //	working
    function resetColorOfEntities(entities) {
        entities.forEach(function (entity) {
            let component = document.getElementById(entity.id);
            if (component == undefined) {
                events.log.error.publish({text: "CanvasManipualtor - resetColorOfEntities - components for entityIds not found"});
                return;
            }
            setColor(component, component.getAttribute("color"));
        });
    }

    //  working
    function hideEntities(entities) {
        entities.forEach(function (entity) {
            let component = document.getElementById(entity.id);
            if (component == undefined) {
                events.log.error.publish({text: "CanvasManipualtor - hideEntities - components for entityIds not found"});
                return;
            }
            setVisibility(component, false)
        });
    }

    //  working
    function showEntities(entities) {
        entities.forEach(function (entity) {
            let component = document.getElementById(entity.id);
            if (component == undefined) {
                events.log.error.publish({text: "CanvasManipualtor - showEntities - components for entityIds not found"});
                return;
            }
            setVisibility(component, true)
        });
    }

    function highlightEntities(entities, color) {
        entities.forEach(function (entity) {
            let component = document.getElementById(entity.id);
            if (component == undefined) {
                events.log.error.publish({text: "CanvasManipualtor - highlightEntities - components for entityIds not found"});
                return;
            }
            setColor(component, color);
        });
    }

    function unhighlightEntities(entities) {
        entities.forEach(function (entity) {
            let component = document.getElementById(entity.id);
            if (component == undefined) {
                events.log.error.publish({text: "CanvasManipualtor - unhighlightEntities - components for entityIds not found"});
                return;
            }
            setColor(component, component.getAttribute("color"));
        });
    }

    //  after clicking an entity fit the camera to show this entity (angle stays the same)
    //  not working
    function flyToEntity(entity) {
        /*document.querySelector("#camera").object3D.position = {x: 1, y: 2, z: 3};
        console.debug(document.querySelector("#camera").object3D.position);*/
    }

    function addElement(element) {
        var addedElements = document.getElementById("addedElements");
        addedElements.appendChild(element);
    }

    function removeElement(element) {
        var addedElements = document.getElementById("addedElements");
        addedElements.removeChild(element);
    }


    //  not working yet
    //  gets called from Mark- and SelectController if specified in the config
    function setCenterOfRotation(entity, setFocus) {
        var centerOfPart = getCenterOfEntity(entity);

        viewpoint.setCenterOfRotation(centerOfPart);

        if (setFocus) {
            var mat = viewarea.getViewMatrix().inverse();

            var from = mat.e3();
            var at = viewarea._pick;
            var up = mat.e1();

            var norm = mat.e0().cross(up).normalize();
            // get distance between look-at point and viewing plane
            var dist = norm.dot(viewarea._pick.subtract(from));

            from = at.addScaled(norm, -dist);
            mat = x3dom.fields.SFMatrix4f.lookAt(from, at, up);

            viewarea.animateTo(mat.inverse(), viewpoint);
        }
    }


    function getCenterOfEntity(entity) {
        var entityPart = getPart(entity);
        var volumeOfPart = entityPart.getVolume();
        var centerOfPart = volumeOfPart.center;

        return centerOfPart;
    }


    //Helper
    function getPart(entity) {
        if (entity.part == undefined) {
            var part = multiPart.getParts([entity.id]);
            entity.part = part;
        }

        return entity.part;
    }

    //	working
    function setColor(object, color) {
        object.setAttribute("material", {
            color: color
        });
    }

    function setTransparency(object, value) {
        object.setAttribute('material', {
            opacity: value
        });
    }


    function setVisibility(object, visibility) {
        object.setAttribute("visible", visibility);
    }

    return {
        initialize: initialize,
        reset: reset,
        colors: colors,

        changeTransparencyOfEntities: changeTransparencyOfEntities,
        resetTransparencyOfEntities: resetTransparencyOfEntities,

        changeColorOfEntities: changeColorOfEntities,
        resetColorOfEntities: resetColorOfEntities,

        hideEntities: hideEntities,
        showEntities: showEntities,

        highlightEntities: highlightEntities,
        unhighlightEntities: unhighlightEntities,

        flyToEntity: flyToEntity,

        addElement: addElement,
        removeElement: removeElement,


        setCenterOfRotation: setCenterOfRotation,
        getCenterOfEntity: getCenterOfEntity,
    };

})();