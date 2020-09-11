var magGlassController = (function () {

    //config parameters	
    var controllerConfig = {
        selectionKeyboardKey: 90 // "z" for zoom
    };

    let magGlassActiveObject = false;

    function initialize(setupConfig) {
        application.transferConfigParams(setupConfig, controllerConfig);

        //Approach followed: https://stackoverflow.com/questions/53905525/aframe-how-to-render-a-camera-to-a-texture
        AFRAME.registerComponent('camrender', {
            'schema': {
                // set FPS
                fps: {
                    type: 'number',
                    default: 60.0
                },
                // set Id of the canvas element used for rendering the camera
                cid: {
                    type: 'string',
                    default: 'camRenderer'
                },
                // set resolution height
                height: {
                    type: 'number',
                    default: 2000
                },
                // set resolution width
                width: {
                    type: 'number',
                    default: 2000
                }
            },
            'update': function (oldData) {
                var data = this.data
                if (oldData.cid !== data.cid) {
                    // Find canvas element to be used for rendering
                    var canvasEl = document.getElementById(this.data.cid);
                    // Create renderer
                    this.renderer = new THREE.WebGLRenderer({
                        antialias: true,
                        canvas: canvasEl
                    });
                    // Set properties for renderer DOM element
                    this.renderer.setPixelRatio(window.devicePixelRatio);
                    this.renderer.domElement.crossorigin = "anonymous";
                };
                if (oldData.width !== data.width || oldData.height !== data.height) {
                    // Set size of canvas renderer
                    this.renderer.setSize(data.width, data.height);
                    this.renderer.domElement.height = data.height;
                    this.renderer.domElement.width = data.width;
                };
                if (oldData.fps !== data.fps) {
                    // Set how often to call tick
                    this.tick = AFRAME.utils.throttleTick(this.tick, 1000 / data.fps, this);
                };
            },
            'tick': function (time, timeDelta) {
                this.renderer.render(this.el.sceneEl.object3D, this.el.object3DMap.camera);
            }
        });

        AFRAME.registerComponent('canvas-updater', {
            dependencies: ['geometry', 'material'],

            tick: function () {
                var el = this.el;
                var material;

                material = el.getObject3D('mesh').material;
                if (!material.map) {
                    return;
                }
                material.map.needsUpdate = true;
            }
        });
    
    }

    function activate() {

        //Just the TopPanel with Name and
        let topPanel = document.getElementById("UI0_topfirstPanel");
        topPanel.setAttribute("style", "padding: 15px;")
        console.log(topPanel)
        let h1 = document.createElement("H1");
        let text1 = document.createTextNode("Magnification Glass");
        let h2 = document.createElement("H2");
        let text2 = document.createTextNode("Press 'z' to activate zoom panel and 'z' again to disable it");
        h1.appendChild(text1);
        h2.appendChild(text2);
        topPanel.appendChild(h1);
        topPanel.appendChild(h2);

        // actionController.actions.mouse.hover.subscribe(downAction); //working for tests
        // actionController.actions.mouse.unhover.subscribe(upAction);  //working for tests
        // actionController.actions.keyboard.key[controllerConfig.selectionKeyboardKey].down.subscribe(downAction); //not yet implemented in AframeActionController.js
        // actionController.actions.keyboard.key[controllerConfig.selectionKeyboardKey].up.subscribe(upAction); //not yet implemented in AframeActionController.js

        //when keyboard events are implemented in AframeActionController.js, replace the following lines with the above ones. 
        //Mind the magGlassActiveObject! (can only disable when active and vice-versa)
        //The current camera is passed on to enable the secondary camera to have the same view angle on the object and revert to the original view and position when removing the magGlass
        document.addEventListener('keydown', logKey);
        function logKey(e) {
            let saveOriginalCamera = document.getElementById("camera");
            console.log("magGlassActiveObject " + magGlassActiveObject)
            if (e.keyCode == controllerConfig.selectionKeyboardKey) { 
                if (magGlassActiveObject === false) {
                    downAction(saveOriginalCamera);
                }else
                if (magGlassActiveObject === true) {
                    upAction(saveOriginalCamera);
                }
            } 
        }
        
    }

    function reset() {
        //no implementation yet for reset
        console.log("reset")
    }


    function downAction(saveOriginalCamera) {
        showMagLens(saveOriginalCamera);
        magGlassActiveObject = true
    }


    function upAction(saveOriginalCamera) {
        hideMagLens(saveOriginalCamera);
        magGlassActiveObject = false 
    }



    function showMagLens(saveOriginalCamera) {

        let canvas = document.getElementById("aframe-canvas");

        //create assets with secondary canvas for closeup camera -> Image for magnifying glass
        var assets = document.createElement('a-assets');
        assets.id = "assetsID";
        var cameraCanvas = document.createElement('canvas');
        cameraCanvas.setAttribute("id", "cam2");
        assets.appendChild(cameraCanvas);
        canvas.appendChild(assets);



        //cameraPosition -> secondary camera position
        var cameraPosition = document.createElement('a-entity');
        cameraPosition.id = "cameraPositionID";
        // cameraPosition.setAttribute("position", saveOriginalCamera.object3D.position);
        cameraPosition.setAttribute('position', {x: saveOriginalCamera.object3D.position.x, y: saveOriginalCamera.object3D.position.y, z: saveOriginalCamera.object3D.position.z});
        cameraPosition.setAttribute("rotation", saveOriginalCamera.object3D.rotation);
        cameraPosition.setAttribute("orbit-camera", "target: 15.0 1.5 15.0; enableDamping: true; dampingFactor: 0.25;rotateSpeed: 0.25; panSpeed: 0.25; invertZoom: true;logPosition: false; minDistance:0; maxDistance:25;");

        //cameraEntity -> secondary camera creation
        var cameraEntity = document.createElement('a-camera');
        cameraEntity.setAttribute("camrender", "cid: cam2");
        cameraEntity.setAttribute("active", "false");
        cameraEntity.setAttribute("wasd-controls-enabled", "false");
        //canvas append the secondary camera
        cameraPosition.appendChild(cameraEntity);
        canvas.appendChild(cameraPosition);


        
        //replace old camera with new camera and magglass in center fixed. Reset to original camera when magglass close
        //create the magnifying Glass containing the view of the camera
        var magGlassPosition = document.createElement('a-entity');
        magGlassPosition.id = "magGlassPositionID"
        magGlassPosition.setAttribute("position", {x: saveOriginalCamera.object3D.position.x, y: saveOriginalCamera.object3D.position.y, z: saveOriginalCamera.object3D.position.z});
        magGlassPosition.setAttribute("rotation", saveOriginalCamera.object3D.rotation);
        magGlassPosition.setAttribute("orbit-camera", "target: 15.0 1.5 15.0;");
        magGlassPosition.setAttribute("look-controls", "false");
        magGlassPosition.setAttribute("zoom", "false");

        var cameraPositionMagGlass = document.createElement('a-entity');
        cameraPositionMagGlass.setAttribute("camera", "");
        cameraPositionMagGlass.setAttribute("position", "0 0 0.07");


        var magGlassRender = document.createElement('a-entity');
        magGlassRender.ID = "magGlassRenderID"
        magGlassRender.setAttribute("geometry", "primitive:plane; width:0.2; height:0.2");
        magGlassRender.setAttribute("material", "shader: flat; src:#cam2; opacity: .99999"); 
        magGlassRender.setAttribute("canvas-updater", "");
        magGlassRender.setAttribute("position", "0 0 -0.1");
        magGlassRender.setAttribute("rotation", "0 0 0");


        cameraPositionMagGlass.appendChild(magGlassRender);
        magGlassPosition.appendChild(cameraPositionMagGlass);
        magGlassPosition.appendChild(magGlassRender);
        canvas.appendChild(magGlassPosition);
        
    }


    function hideMagLens(saveOriginalCamera) {

        let assets = document.getElementById("assetsID");
        let cameraPosition = document.getElementById("cameraPositionID");
        let magGlassPosition = document.getElementById("magGlassPositionID");

        let canvas = document.getElementById("aframe-canvas");

        //removeMagGlass Elements from canvas
        canvas.removeChild(assets);
        canvas.removeChild(cameraPosition);
        canvas.removeChild(magGlassPosition);


        //a-frame attributes of format: position="0 0 0 " have to be parsed with .getAttributeNode("xx").value (else is "0")
        let backToOriginalCamera = document.createElement('a-entity');
        backToOriginalCamera.setAttribute("id", "camera")
        backToOriginalCamera.setAttribute("camera", "fov: 80; zoom: 1;");
        backToOriginalCamera.setAttribute("position", saveOriginalCamera.object3D.position);
        backToOriginalCamera.setAttribute("rotation", saveOriginalCamera.object3D.rotation);
        backToOriginalCamera.setAttribute("orbit-camera", "target: 15.0 1.5 15.0; enableDamping: true; dampingFactor: 0.25;rotateSpeed: 0.25; panSpeed: 0.25; invertZoom: true;logPosition: false; minDistance:0; maxDistance:1000;");
        backToOriginalCamera.setAttribute("mouse-cursor", "");

        canvas.appendChild(backToOriginalCamera);
    }

    return {
        initialize: initialize,
        reset: reset,
        activate: activate
    };
})();