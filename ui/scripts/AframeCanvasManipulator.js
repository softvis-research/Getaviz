var canvasManipulator = (function () {

    const colors = {
        darkred: "darkred",
        red: "red",
        black: "black",
        orange: "orange",
        darkorange: "darkorange"
    };

    const animations = {
        expanding: "Expanding",
        flashing: "Flashing",
        rotation: "Rotation"
    }

    let scene = {};

    let entityEffectMap = new Map();

    let hiddenEntitiesMap = new Map();

    let notificationCallbackQueue = [];


    function initialize() {

        scene = document.querySelector("a-scene");

        //this is a workaround for a bug of a-frame
        //if the window size is changed from/to max size,
        //the a-scene resize-handler won't be called properly
        //so we call the resize-handler here again
        window.addEventListener("resize", function() { setTimeout(resizeScene, 100) });

        // this component serves as a crutch to tie a callback to AFrame finishing the next render step
        // some operations with entities rely on render data being present in the DOM
        AFRAME.registerComponent('notify-on-render', {
            tock: function (time, timeDelta) {
                if (notificationCallbackQueue.length > 0) {
                    for (const callback of notificationCallbackQueue) {
                        callback();
                    }
                    notificationCallbackQueue = [];
                }
            }
        });
    }

    function reset() {

    }

    function resizeScene() {
        scene.resize();
    }

    function changeTransparencyOfEntities(entities, transparency, controller) {
        entities.forEach(function (entity2) {
            //  getting the entity again here, because without it the check if originalTransparency is defined fails sometimes
            const entity = model.getEntityById(entity2.id);
            if (entity === undefined) {
                return;
            }

            const component = document.getElementById(entity.id);
            if (component === undefined) {
                events.log.error.publish({ text: "CanvasManipulator - changeTransparencyOfEntities - components for entityIds not found" });
                return;
            }

            updateEntityEffectMap(entity, "transparency");

            const transparencyList = entityEffectMap.get(entity.id).get("transparency");

            if (transparencyList.length === 0) {
                let opacity = component.getAttribute("material").opacity;
                opacity = (opacity === undefined) ? 1 : opacity;

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

            setTransparency(component, transparency);
        });
    }

    function resetTransparencyOfEntities(entities, controller) {
        entities.forEach(function (entity) {

            const entityEffect = entityEffectMap.get(entity.id);
            if (!entityEffect) {
                return;
            }

            const transparencyList = entityEffect.get("transparency");
            //just original transparency => nothing to do
            if (!transparencyList || transparencyList.length <= 1) {
                return;
            }

            const transparencyEffectIndex = transparencyList.findIndex(transparencyEffect => transparencyEffect.controller == controller.name);

            //controller not affected the transparency
            if (transparencyEffectIndex === -1) {
                return;
            }

            transparencyList.splice(transparencyEffectIndex, 1);

            if (transparencyEffectIndex === transparencyList.length) {
                const component = document.getElementById(entity.id);
                if (!component) {
                    events.log.error.publish({ text: "CanvasManipulator - resetTransparencyOfEntities - components for entityIds not found" });
                    return;
                }
                setTransparency(component, transparencyList[transparencyList.length - 1].value);
            }
        });
    }

    function startAnimation({ animation, entities, period, scale, flashingColor } = {}) {
        entities.forEach(function (entity) {
            if (!entity) {
                return;
            }
            const component = document.getElementById(entity.id);
            if (!component) {
                events.log.error.publish({ text: "CanvasManipulator - startAnimation - component for entityId not found" });
                return;
            }

            switch (animation) {
                case animations.expanding:
                    const scaleVector = scale + " " + scale + " " + scale;

                    component.setAttribute("animation__expanding",
                        "property: scale; from: 1 1 1; to: " + scaleVector + "; dur: " + period + "; loop: true; dir: alternate");
                    break;

                case animations.rotation:
                    component.setAttribute("animation__yoyo",
                        "property: rotation; dur: " + period + "; easing: easeInOutSine; loop: true; to: 0 360 0");
                    break;

                case animations.flashing:
                    const originalColor = component.getAttribute("color");
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
            if (!entity) {
                return;
            }
            const component = document.getElementById(entity.id);
            if (!component) {
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
                    const originalColor = component.getAttribute("color");

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
            if (!entity) {
                return;
            }

            const component = document.getElementById(entity.id);
            if (!component) {
                events.log.error.publish({ text: "CanvasManipulator - changeColorOfEntities - components for entityIds not found" });
                return;
            }

            updateEntityEffectMap(entity, "color");

            const colorList = entityEffectMap.get(entity.id).get("color");

            if (colorList.length === 0) {
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

            setColor(component, color);
        }
        );
    }

    function resetColorOfEntities(entities, controller) {
        entities.forEach(function (entity) {

            const entityEffect = entityEffectMap.get(entity.id);
            if (!entityEffect) {
                return;
            }

            const colorList = entityEffect.get("color");
            //just original color => nothing to do
            if (colorList.length <= 1) {
                return;
            }

            const colorEffectIndex = colorList.findIndex(colorEffect => colorEffect.controller == controller.name);

            //controller not affected the color
            if (colorEffectIndex === -1) {
                return;
            }

            colorList.splice(colorEffectIndex, 1);

            if (colorEffectIndex === colorList.length) {
                const component = document.getElementById(entity.id);
                if (!component) {
                    events.log.error.publish({ text: "CanvasManipulator - resetColorOfEntities - components for entityIds not found" });
                    return;
                }
                setColor(component, colorList[colorList.length - 1].value);
            }

        });
    }

    function setColor(object, color) {
        if (color === colors.darkred) {
            color = colors.red;
        }
        const colorValues = color.split(" ");
        if (colorValues.length == 3) {
            color = numbersToHexColor(colorValues);
        }
        object.setAttribute("color", color);
    }

    function numbersToHexColor(rgbArray) {
        return "#" + rgbArray.map(value => parseInt(value).toString(16).padStart(2, "0")).join("");
    }

    function hideEntities(entities, controller) {
        entities.forEach(function (entity) {
            const component = document.getElementById(entity.id);
            if (component === null) {
                events.log.error.publish({ text: "CanvasManipulator - hideEntities - components for entityIds not found" });
                return;
            }

            hiddenEntitiesMap.set(entity.id, component);
            component.remove();
        });

    }

    function showEntities(entities, controller) {
        const sceneEl = document.querySelector('a-scene');

        entities.forEach(function (entity) {
            if (!hiddenEntitiesMap.has(entity.id)) {
                events.log.error.publish({ text: "CanvasManipulator - showEntities - components for entityIds not found" });
                return;
            }

            const component = hiddenEntitiesMap.get(entity.id);
            hiddenEntitiesMap.delete(entity.id);

            // removed elements seemingly can't be simply re-inserted, so recreate the element instead
            const entityEl = document.createElement(component.tagName);
            for (const curAttribute of component.attributes) {
                entityEl.setAttribute(curAttribute.name, curAttribute.value);
            }
            // flushing is necessary to make AFrame apply the attribute changes (position in particular) to the DOM
            entityEl.flushToDOM();
            sceneEl.appendChild(entityEl);
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
        const addedElements = document.getElementById("addedElements");
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
        const object = document.getElementById(entity.id).object3DMap.mesh;
        const center = new THREE.Vector3(object.position.x, object.position.y, object.position.z);

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

    function mapAframeDataToHTML(element) {
        const stringProperties = ['id', 'position', 'height', 'width', 'depth', 'radius', 'color', 'src', 'gltf-model', 'scale', 'rotation'];
        const boolProperties = ['shadow'];
        const htmlProperties = stringProperties.map(prop => (element[prop] ? `${prop}="${element[prop]}"` : ``)).filter(s => s.length)
            .concat(boolProperties.map(prop => (element[prop] ? prop : ``)).filter(s => s.length))
            .join('\n\t');
        return `<${element.shape} ${htmlProperties}>\n</${element.shape}>`;
    }

    function addElementsFromAframeData(dataArray) {
        const generatedHtml = dataArray.map(mapAframeDataToHTML);
        document.getElementById(canvasId).insertAdjacentHTML('beforeend', generatedHtml);
    }

    function loadAsHiddenFromAframeData(dataArray) {
        for (const jsonElement of dataArray) {
            // helper element to transform the HTML string into an HTMLElement
            const template = document.createElement('template');
            template.innerHTML = mapAframeDataToHTML(jsonElement);
            hiddenEntitiesMap.set(jsonElement.id, template.content.firstChild);
        }
    }

    async function waitForRenderOfElement(element) {
        const domElement = document.getElementById(element.id);
        // the callback gets inserted into a queue, where it will be called by the notify-on-render component, resolving this promise
        await new Promise((resolve, reject) => {
            const callback = (() => {
                domElement.removeAttribute('notify-on-render');
                resolve();
            });
            notificationCallbackQueue.push(callback);
            domElement.setAttribute('notify-on-render', '');
            // A-Frame renders many times per second, so if it's been a second without a call, something probably went wrong
            window.setTimeout(() => reject(), 1000);
        }).catch(() => {
            events.log.error.publish({ text: `CanvasManipulator - waitForRenderOfElement on ${element.id} - timed out` });
        });
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
        numbersToHexColor: numbersToHexColor,

        hideEntities: hideEntities,
        showEntities: showEntities,

        highlightEntities: highlightEntities,
        unhighlightEntities: unhighlightEntities,

        flyToEntity: flyToEntity,

        addElement: addElement,
        removeElement: removeElement,

        setCenterOfRotation: setCenterOfRotation,
        getCenterOfEntity: getCenterOfEntity,

        addElementsFromAframeData: addElementsFromAframeData,
        loadAsHiddenFromAframeData: loadAsHiddenFromAframeData,

        waitForRenderOfElement: waitForRenderOfElement
    };

})
    ();