var canvasManipulator = (function () {

    var colors = {
        darkred: "darkred",
        red: "red",
        black: "black",
        orange: "orange",
        darkorange: "darkorange"
    };

    var scene = {};

    var initialCameraView = {};

    function initialize() {

        scene = document.querySelector("a-scene");

        initialCameraView.target = globalCamera.target;
        initialCameraView.position = globalCamera.object.position;
        initialCameraView.spherical = globalCamera.spherical;

        AFRAME.registerComponent('set-aframe-attributes', {
            // schema defines properties of element
            schema: {
                shape: { type: 'string', default: '' },
                id: { type: 'string', default: '' },
                class: { type: 'string', default: '' },
                position: { type: 'string', default: '' },
                width: { type: 'string', default: '' },
                height: { type: 'string', default: '' },
                depth: { type: 'string', default: '' },
                radius: { type: 'string', default: '' },
                color: { type: 'string', default: '' },            
                shader: { type: 'string', default: '' },
                shadow: { type: 'string', default: '' },
                flatShading: { type: 'string', default: '' }
            },
    
            init: function () {
                // This will be called after the entity has been properly attached and loaded.
                this.attrValue = ''; // imported payload is in this.data, so we don't need this one
                Object.keys(this.schema).forEach(key => {
                    this.el.setAttribute(`${key}`, this.data[key]);                
                })

                // Adjust visibility
                // let visible = this.data['checked'] ? true : false; // set to visible, because parent node was checked
                this.el.setAttribute('visible', false);
            }
        });
    }

    function reset() {
        let offset = new THREE.Vector3();
        offset.subVectors(initialCameraView.target, globalCamera.target).multiplyScalar(globalCamera.data.panSpeed);
        globalCamera.panOffset.add(offset);

        globalCamera.sphericalDelta.phi = 0.25 * (initialCameraView.spherical.phi - globalCamera.spherical.phi);
        globalCamera.sphericalDelta.theta = 0.25 * (initialCameraView.spherical.theta - globalCamera.spherical.theta);

        globalCamera.scale = initialCameraView.spherical.radius/globalCamera.spherical.radius;
    }

    // function 

    function changeTransparencyOfEntities(entities, value) {
        entities.forEach(function (entity2) {
            //  getting the entity again here, because without it the check if originalTransparency is defined fails sometimes
            let entity = model.getEntityById(entity2.id);
            if (entity == undefined) {
                return;
            }
            let component = document.getElementById(entity.id);
            if (component == undefined) {
                events.log.error.publish({text: "CanvasManipualtor - changeTransparencyOfEntities - components for entityIds not found"});
                return;
            }
            if (entity.originalTransparency === undefined) {
                entity.originalTransparency = {};
                entity.currentTransparency = {};
                // If elements are new, they probably don't have opacity parameter
                if (component.getAttribute("material")) {
                    if(component.getAttribute("material").opacity) {
                        entity.originalTransparency = 1 - component.getAttribute("material").opacity;
                    }
                } else {
                    entity.originalTransparency = 0;
                }
            }
            entity.currentTransparency = value;
            setTransparency(component, value);
        });
    }

    function resetTransparencyOfEntities(entities) {
        entities.forEach(function (entity) {
            let component = document.getElementById(entity.id);
            if (component == undefined) {
                events.log.error.publish({text: "CanvasManipualtor - resetTransparencyOfEntities - components for entityIds not found"});
                return;
            }
            if (!(entity.originalTransparency == undefined)) {
                entity.currentTransparency = entity.originalTransparency;
                setTransparency(component, entity.originalTransparency);
            }
        });
    }

    function changeColorOfEntities(entities, color) {
        entities.forEach(function (entity) {
                if (!(entity == undefined)) {
                    var component = document.getElementById(entity.id);
                }
                if (component == undefined) {
                    events.log.error.publish({text: "CanvasManipualtor - changeColorOfEntities - components for entityIds not found"});
                    return;
                }
                if (entity.originalColor == undefined) {
                    entity.originalColor = component.getAttribute("color");
                }
                entity.currentColor = color;
                setColor(component, color);
            }
        );
    }

    function resetColorOfEntities(entities) {
        entities.forEach(function (entity) {
            let component = document.getElementById(entity.id);
            if (component == undefined) {
                events.log.error.publish({text: "CanvasManipualtor - resetColorOfEntities - components for entityIds not found"});
                return;
            }
            if (entity.originalColor) {
                entity.currentColor = entity.originalColor;
                setColor(component, entity.originalColor);
            }
        });
    }

    function setColor(object, color) {
        color == colors.darkred ? color = colors.red : color = color;
        let colorValues = color.split(" ");
        if (colorValues.length == 3) {
            color = "#" + parseInt(colorValues[0]).toString(16) + "" + parseInt(colorValues[1]).toString(16) + "" + parseInt(colorValues[2]).toString(16);
        }
        object.setAttribute("color", color);
    }

    function hideEntities(entities) {
        entities.forEach(async (entity) => {
            // await aframeModelLoadController.checkAndLoadNodeById(entity.id);
            let component = document.getElementById(entity.id);
            if (component == undefined) {
                events.log.error.publish({text: "CanvasManipualtor - hideEntities - components for entityIds not found"});
                return;
            }
            setVisibility(component, false)
        });
    }

    function showEntities(entities) {
        entities.forEach(async (entity) => {
            // await aframeModelLoadController.checkAndLoadNodeById(entity.id);
            let component = document.getElementById(entity.id);
            if (component == undefined) {
                events.log.error.publish({text: "CanvasManipualtor - showEntities - components for entityIds not found"});
                return;
            }
            setVisibility(component, true)
        });
    }

    function highlightEntities(entities, color) {
        entities.forEach(function (entity2) {
            //  getting the entity again here, because without it the check if originalTransparency is defined fails sometimes
            let entity = model.getEntityById(entity2.id);
            let component = document.getElementById(entity.id);
            if (component == undefined) {
                events.log.error.publish({text: "CanvasManipualtor - highlightEntities - components for entityIds not found"});
                return;
            }
            if (entity.originalColor == undefined) {
                entity.originalColor = component.getAttribute("color");
                entity.currentColor = entity.originalColor;
            }
            if (entity["originalTransparency"] === undefined) {
                // in case "material".opacity is undefined originalTransparency gets set to 0 which would be the default value anyways
                entity.originalTransparency = {};
                entity.currentTransparency = {};
                if(component.getAttribute("material").opacity) {
                    entity.originalTransparency = 1 - component.getAttribute("material").opacity;
                } else entity.originalTransparency = 0;
                entity.currentTransparency = entity.originalTransparency;
            }
            setColor(component, color);
            setTransparency(component, 0);
        });
    }

    function unhighlightEntities(entities) {
        entities.forEach(function (entity) {
            let component = document.getElementById(entity.id);
            if (component == undefined) {
                events.log.error.publish({text: "CanvasManipualtor - unhighlightEntities - components for entityIds not found"});
                return;
            }
            setTransparency(component, entity.currentTransparency);
            setColor(component, entity.currentColor);
        });
    }

    function flyToEntity(entity) {
        setCenterOfRotation(entity);
        let object = document.getElementById(entity.id);
        let boundingSphereRadius = object.object3DMap.mesh.geometry.boundingSphere.radius;
        globalCamera.scale = boundingSphereRadius/globalCamera.spherical.radius;
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
        offset.subVectors(getCenterOfEntity(entity), globalCamera.target).multiplyScalar(globalCamera.data.panSpeed);
        globalCamera.panOffset.add(offset);
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
        object.setAttribute("visible", visibility);
    }

    function getElementIds() { 
        let sceneArray = document.querySelectorAll('.city-element')
        let elementIds = [];
        sceneArray.forEach(sceneElement => {
            elementIds.push(sceneElement.id);
        });

        return elementIds;
    }

    function appendAframeElementWithProperties(element) {
        let aframeProperty = element.row[0].aframeProperty;
        let aframeObject = JSON.parse(`${aframeProperty}`); // create an object

        let entityEl = document.createElement(`${aframeObject.shape}`);
        entityEl.setAttribute('set-aframe-attributes', {...aframeObject}); // this attributes will be set after element is created
        let sceneEl = document.querySelector('a-scene');
        sceneEl.appendChild(entityEl);

        neo4jModelLoadController.updateLoadSpinner(neo4jModelLoadController.loaderController.loaded);
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

        getElementIds: getElementIds,

        appendAframeElementWithProperties: appendAframeElementWithProperties
    };

})
();