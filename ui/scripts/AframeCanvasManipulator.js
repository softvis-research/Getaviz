var canvasManipulator = (function () {

    var colors = {
        darkred: "darkred",
        red: "red",
        black: "black",
        orange: "orange",
        darkorange: "darkorange"
    };

    var animations = {
        expanding: "Expanding",
        flashing: "Flashing",
        rotation: "Rotation"
    }

    var scene = {};

    var entityEffectMap = new Map();

    var initialCameraView = {};

    function initialize() {

        scene = document.querySelector("a-scene");

        /*
        initialCameraView.target = globalCamera.target;
        initialCameraView.position = globalCamera.object.position;
        initialCameraView.spherical = globalCamera.spherical;
        */

        window.addEventListener("resize", function(event) { setTimeout(resizeScene, 100) });
    }

    function reset() {
        /*
        let offset = new THREE.Vector3();
        offset.subVectors(initialCameraView.target, globalCamera.target).multiplyScalar(globalCamera.data.panSpeed);
        globalCamera.panOffset.add(offset);

        globalCamera.sphericalDelta.phi = 0.25 * (initialCameraView.spherical.phi - globalCamera.spherical.phi);
        globalCamera.sphericalDelta.theta = 0.25 * (initialCameraView.spherical.theta - globalCamera.spherical.theta);

        globalCamera.scale = initialCameraView.spherical.radius/globalCamera.spherical.radius;
        */
    }

    function resizeScene() {
        document.querySelector('a-scene').resize();
    }

    function changeTransparencyOfEntities(entities, transparency, controller) {
        entities.forEach(function (entity2) {
            //  getting the entity again here, because without it the check if originalTransparency is defined fails sometimes
            let entity = model.getEntityById(entity2.id);
            if (entity === undefined) {
                return;
            }

            let component = document.getElementById(entity.id);
            if (component == undefined) {
                events.log.error.publish({ text: "CanvasManipulator - changeTransparencyOfEntities - components for entityIds not found" });
                return;
            }

            updateEntityEffectMap(entity, "transparency");

            var transparencyList = entityEffectMap.get(entity.id).get("transparency");

            if (transparencyList.length == 0) {
                var opacity = component.getAttribute("material").opacity;
                opacity = (opacity == undefined) ? 1 : opacity;

                transparencyList.push(
                    {
                        controller: "original",
                        value: 1 - opacity
                    }
                );
            }

            transparencyList.push(
                {
                    controller: controller.name,
                    value: transparency
                }
            );

            // if (entity.originalTransparency === undefined) {
            //     entity.originalTransparency = {};
            //     entity.currentTransparency = {};
            //     if (component.getAttribute("material").opacity) {
            //         entity.originalTransparency = 1 - component.getAttribute("material").opacity;
            //     }
            // }
            // entity.currentTransparency = transparency;

            setTransparency(component, transparency);
        });
    }

    function resetTransparencyOfEntities(entities, controller) {
        entities.forEach(function (entity) {

            var transparencyList = entityEffectMap.get(entity.id).get("transparency");

            //just original transparency => nothing to do
            if (transparencyList.length <= 1) {
                return;
            }

            var transparencyEffectIndex = transparencyList.findIndex(transparencyEffect => transparencyEffect.controller = controller.name);

            //controller not affected the transparency
            if (transparencyEffectIndex == -1) {
                return;
            }

            transparencyList.splice(transparencyEffectIndex, 1);

            if (transparencyEffectIndex == transparencyList.length) {
                let component = document.getElementById(entity.id);
                if (component == undefined) {
                    events.log.error.publish({ text: "CanvasManipulator - resetTransparencyOfEntities - components for entityIds not found" });
                    return;
                }
                setTransparency(component, transparencyList[transparencyList.length - 1].value);
            }

            // if (!(entity.originalTransparency == undefined)) {
            //     entity.currentTransparency = entity.originalTransparency;
            // }
        });
    }

    function startAnimation({ animation, entities, period, scale, flashingColor } = {}) {
        entities.forEach(function (entity) {
            if (entity !== undefined) {
                var component = document.getElementById(entity.id);
            }
            if (component == undefined) {
                events.log.error.publish({ text: "CanvasManipulator - startAnimation - component for entityId not found" });
                return;
            }

            switch (animation) {
                case animations.expanding:
                    var scaleVector = scale + " " + scale + " " + scale;

                    component.setAttribute("animation__expanding",
                        "property: scale; from: 1 1 1; to: " + scaleVector + "; dur: " + period + "; loop: true; dir: alternate");
                    break;

                case animations.rotation:
                    component.setAttribute("animation__yoyo",
                        "property: rotation; dur: " + period + "; easing: easeInOutSine; loop: true; to: 0 360 0");
                    break;

                case animations.flashing:
                    var originalColor = component.getAttribute("color");
                    component.setAttribute("color-before-animation", originalColor);

                    component.setAttribute("animation__color",
                        "property: components.material.material.color; type: color; from: " + originalColor +
                        "; to: " + flashingColor + "; dur: " + period + "; loop: true; dir: alternate");

                default:
                    break;
            }
        });
    }

    function stopAnimation({ animation, entities } = {}) {
        entities.forEach(function (entity) {
            if (entity !== undefined) {
                var component = document.getElementById(entity.id);
            }
            if (component == undefined) {
                events.log.error.publish({ text: "CanvasManipulator - stopAnimation - component for entityId not found" });
                return;
            }

            switch (animation) {
                case animations.expanding:
                    component.removeAttribute("animation__expanding");
                    component.setAttribute("scale", "1 1 1");
                    break;

                case animations.rotation:
                    component.removeAttribute("animation__yoyo");
                    component.setAttribute("rotation", "0 0 0");
                    break;

                case animations.flashing:
                    var originalColor = component.getAttribute("color");

                    if (originalColor !== null) {
                        component.removeAttribute("animation__color");

                        component.setAttribute("animation__color_off",
                            "property: components.material.material.color; type: color; from: " + originalColor +
                            "; to: " + originalColor + "; dur: 0; loop: false");
                    }

                default:
                    break;
            }
        });
    }

    function updateEntityEffectMap(entity, effect) {
        if (!entityEffectMap.has(entity.id)) {
            entityEffectMap.set(entity.id, new Map());
        }

        if (!entityEffectMap.get(entity.id).has(effect)) {
            entityEffectMap.get(entity.id).set(effect, new Array);
        }
    }

    function changeColorOfEntities(entities, color, controller) {
        entities.forEach(function (entity) {
            if (entity == undefined) {
                return;
            }

            let component = document.getElementById(entity.id);
            if (component == undefined) {
                events.log.error.publish({ text: "CanvasManipulator - changeColorOfEntities - components for entityIds not found" });
                return;
            }

            updateEntityEffectMap(entity, "color");

            var colorList = entityEffectMap.get(entity.id).get("color");

            if (colorList.length == 0) {
                colorList.push(
                    {
                        controller: "original",
                        value: component.getAttribute("color")
                    }
                );
            }

            colorList.push(
                {
                    controller: controller.name,
                    value: color
                }
            );

            // if (entity.originalColor == undefined) {
            //     entity.originalColor = component.getAttribute("color");
            // }
            // entity.currentColor = color;

            setColor(component, color);
        }
        );
    }

    function resetColorOfEntities(entities, controller) {
        entities.forEach(function (entity) {

            var colorList = entityEffectMap.get(entity.id).get("color");

            //just original color => nothing to do
            if (colorList.length <= 1) {
                return;
            }

            var colorEffectIndex = colorList.findIndex(colorEffect => colorEffect.controller == controller.name);

            //controller not affected the color
            if (colorEffectIndex == -1) {
                return;
            }

            colorList.splice(colorEffectIndex, 1);

            if (colorEffectIndex == colorList.length) {
                let component = document.getElementById(entity.id);
                if (component == undefined) {
                    events.log.error.publish({ text: "CanvasManipulator - resetColorOfEntities - components for entityIds not found" });
                    return;
                }
                setColor(component, colorList[colorList.length - 1].value);
            }

            // if (entity.originalColor) {
            //     entity.currentColor = entity.originalColor;
            //     setColor(component, entity.originalColor);
            // }
        });
    }

    function setColor(object, color) {
        color == colors.darkred ? color = colors.red : color = color;
        let colorValues = color.split(" ");
        if (colorValues.length == 3) {
            color = "#" + parseInt(colorValues[0]).toString(16).padStart(2, "0") + parseInt(colorValues[1]).toString(16).padStart(2, "0") + parseInt(colorValues[2]).toString(16).padStart(2, "0");
        }
        object.setAttribute("color", color);
    }

    function hideEntities(entities, controller) {
        changeVisibilityOfEntities(entities, false, controller);
    }

    function showEntities(entities, controller) {
        changeVisibilityOfEntities(entities, true, controller);
    }

    function changeVisibilityOfEntities(entities, targetValue, controller) {
        entities.forEach(function (entity) {
            if (entity == undefined) {
                return;
            }

            let component = document.getElementById(entity.id);
            if (component == undefined) {
                events.log.error.publish({ text: "CanvasManipulator - changeVisibilityOfEntities - components for entityIds not found" });
                return;
            }

            updateEntityEffectMap(entity, "visibility");

            var visibilityList = entityEffectMap.get(entity.id).get("visibility");

            var visible = component.object3D.visible;

            if (visibilityList.length == 0) {
                visibilityList.push(
                    {
                        controller: "original",
                        value: visible
                    }
                );
            } else {
                var visibilityEffectIndex = visibilityList.findIndex(visibilityEffect => visibilityEffect.controller == controller.name);

                //remove old visibility entry of this controller
                if (visibilityEffectIndex != -1) {
                    visibilityList.splice(visibilityEffectIndex, 1);
                }
            }

            visibilityList.push(
                {
                    controller: controller.name,
                    value: targetValue
                }
            );

            //length is always at least 2 (original value and target value)
            if (visible != targetValue) {
                setVisibility(component, targetValue);
            }
        });
    }

    function highlightEntities(entities, color, controller) {
        changeColorOfEntities(entities, color, controller);
        changeTransparencyOfEntities(entities, 0, controller);
    }

    function unhighlightEntities(entities, controller) {
        resetColorOfEntities(entities, controller);
        resetTransparencyOfEntities(entities, controller);
    }

    function flyToEntity(entity) {
        setCenterOfRotation(entity);
        let object = document.getElementById(entity.id);
        let boundingSphereRadius = object.object3DMap.mesh.geometry.boundingSphere.radius;
        //globalCamera.scale = boundingSphereRadius/globalCamera.spherical.radius;
    }

    function addElement(element) {
        var addedElements = document.getElementById("addedElements");
        addedElements.appendChild(element);
    }

    function removeElement(element) {
        element.parentNode.removeChild(element);
    }


    function setCenterOfRotation(entity) {
        let offset = new THREE.Vector3();
        //offset.subVectors(getCenterOfEntity(entity), globalCamera.target).multiplyScalar(globalCamera.data.panSpeed);
        //globalCamera.panOffset.add(offset);
    }

    function getCenterOfEntity(entity) {
        var center = new THREE.Vector3();
        var object = document.getElementById(entity.id).object3DMap.mesh;
        center.x = object.geometry.boundingSphere.center["x"];
        center.y = object.geometry.boundingSphere.center["y"];
        center.z = object.geometry.boundingSphere.center["z"];
        return object.localToWorld(center);
    }

    function setTransparency(object, value) {
        object.setAttribute('material', {
            opacity: 1 - value
        });
    }


    function setVisibility(object, visibility) {
        object.object3D.visible = visibility;
    }

    function getElementIds() {
        let sceneArray = Array.from(scene.children);
        sceneArray.shift(); // so camera entity needs to be first in model.html
        sceneArray.pop();  // last element is of class "a-canvas"
        let elementIds = [];
        sceneArray.forEach(function (object) {
            elementIds.push(object.id);
        });
        return elementIds;
    }

    return {
        initialize: initialize,
        reset: reset,
        colors: colors,

        resizeScene: resizeScene,

        changeTransparencyOfEntities: changeTransparencyOfEntities,
        resetTransparencyOfEntities: resetTransparencyOfEntities,

        startAnimation: startAnimation,
        stopAnimation: stopAnimation,

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

        getElementIds: getElementIds
    };

})
    ();