var canvasMarkController = (function () {


	let SELECTION_MODES = {
		UP: "UP",
		DOWN: "DOWN",
		DURATION: "DURATION"
	}

	var markingColor = "0 1 0";

	//config parameters	
	var controllerConfig = {
		setCenterOfRotation: false,
		//markingColor: "green",
		selectionMouseKey: 2,
		selectionMode: SELECTION_MODES.UP,
		selectionDurationSeconds: 0.5,
		selectionMoveAllowed: false,
		showProgressBar: false,
	}

	//let downActionEventObject;
	var downActionEventObject;

	function initialize(setupConfig) {

		application.transferConfigParams(setupConfig, controllerConfig);

	}

	function activate() {

		actionController.actions.mouse.key[controllerConfig.selectionMouseKey].down.subscribe(downAction);
		actionController.actions.mouse.key[controllerConfig.selectionMouseKey].up.subscribe(upAction);
		actionController.actions.mouse.key[controllerConfig.selectionMouseKey].during.subscribe(duringAction);
		actionController.actions.mouse.move.subscribe(mouseMove);

		//DUMMY
		actionController.actions.keyboard.key[87].down.subscribe(getViewPoint); "W"
		actionController.actions.keyboard.key[83].down.subscribe(setViewPoint); "S"
		//DUMMY

		events.marked.on.subscribe(onEntityMarked);
		events.marked.off.subscribe(onEntityUnmarked);
	}

	function reset() {
		//let markedEntities = events.marked.getEntities();
		var markedEntities = events.marked.getEntities();

		canvasManipulator.resetColorOfEntities(markedEntities, { name: "canvasMarkController" });
	}

	//DUMMY
	var myViewMatrix;
	var myCenterRotation;
	function getViewPoint() {

		//get reference of x3dom objects
		var x3domRuntime = document.getElementById('x3dElement').runtime;
		var viewarea = x3domRuntime.canvas.doc._viewarea;
		var viewpoint = viewarea._scene.getViewpoint();

		myViewMatrix = viewarea.getViewMatrix();
		console.log(myViewMatrix);
		myCenterRotation = viewpoint.getCenterOfRotation();
		console.log(myCenterRotation);
	}
	function setViewPoint() {
		//get reference of x3dom objects
		var x3domRuntime = document.getElementById('x3dElement').runtime;
		var viewarea = x3domRuntime.canvas.doc._viewarea;
		var viewpoint = viewarea._scene.getViewpoint();

		viewpoint.setView(myViewMatrix)
		viewarea._needNavigationMatrixUpdate = true;
		viewpoint.setCenterOfRotation(myCenterRotation);
	}
	//DUMMY

	function downAction(eventObject, timestamp) {

		downActionEventObject = null;

		if (!eventObject.entity) {
			return;
		}

		if (controllerConfig.selectionMode === "DOWN") {
			handleOnClick(eventObject);
			return;
		}

		downActionEventObject = eventObject;

		if (controllerConfig.selectionMode === "DURATION" && controllerConfig.showProgressBar) {
			showProgressBar(eventObject);
		}
	}

	function upAction(eventObject) {

		if (!downActionEventObject) {
			return;
		}

		if (controllerConfig.selectionMode === "UP") {
			handleOnClick(downActionEventObject);
			return;
		}

		if (controllerConfig.selectionMode === "DURATION" && controllerConfig.showProgressBar) {
			hideProgressBar();
		}
	}

	function duringAction(eventObject, timestamp, timeSinceStart) {

		if (!downActionEventObject) {
			return;
		}

		if (controllerConfig.selectionMode !== "DURATION") {
			return;
		}

		if (timeSinceStart > (1000 * controllerConfig.selectionDurationSeconds)) {
			hideProgressBar();
			handleOnClick(downActionEventObject);
			downActionEventObject = null;
			return;
		}
	}

	function mouseMove(eventObject, timestamp) {
		if (!downActionEventObject) {
			return;
		}

		if (!controllerConfig.selectionMoveAllowed) {
			hideProgressBar();
			downActionEventObject = null;
		}
	}


	function handleOnClick(eventObject) {

		//let applicationEvent = {
		var applicationEvent = {
			sender: canvasMarkController,
			entities: [eventObject.entity]
		};

		if (eventObject.entity.marked) {
			events.marked.off.publish(applicationEvent);
		} else {
			events.marked.on.publish(applicationEvent);
		}

		//center of rotation
		if (controllerConfig.setCenterOfRotation) {
			canvasManipulator.setCenterOfRotation(eventObject.entity);
		}
	}





	function onEntityMarked(applicationEvent) {
		applicationEvent.entities.forEach(function (entity) {
			if (entity.hovered) {
				canvasManipulator.unhighlightEntities([entity], { name: "canvasMarkController" });
			}
			canvasManipulator.changeColorOfEntities([entity], controllerConfig.markingColor, { name: "canvasMarkController" });
		});
	}

	function onEntityUnmarked(applicationEvent) {
		applicationEvent.entities.forEach(function (entity) {
			canvasManipulator.resetColorOfEntities([entity], { name: "canvasMarkController" });
		});
	}

	function showProgressBar(eventObject) {

		let canvas = document.getElementById("canvas");

		let progressBarDivElement = document.createElement("DIV");
		progressBarDivElement.id = "progressBarDiv";

		canvas.appendChild(progressBarDivElement);

		let progressBar = $('#progressBarDiv');

		progressBar.jqxProgressBar({
			width: 250,
			height: 30,
			value: 100,
			animationDuration: controllerConfig.selectionDurationSeconds * 1000,
			template: "success"
		});


		progressBar.css("top", eventObject.layerY + 10 + "px");
		progressBar.css("left", eventObject.layerX + 10 + "px");

		progressBar.css("z-index", "1");
		progressBar.css("position", "absolute");

		progressBar.css("width", "250px");
		progressBar.css("height", "30px");

		progressBar.css("display", "block");

	}

	function hideProgressBar() {

		//let progressBarDivElement = document.getElementById("progressBarDiv");
		var progressBarDivElement = document.getElementById("progressBarDiv");

		if (!progressBarDivElement) {
			return;
		}

		//let canvas = document.getElementById("canvas");
		var canvas = document.getElementById("canvas");
		canvas.removeChild(progressBarDivElement);
	}


	return {
		initialize: initialize,
		reset: reset,
		activate: activate,
		onEntityMarked: onEntityMarked,
		onEntityUnmarked: onEntityUnmarked,
		SELECTION_MODES: SELECTION_MODES
	};
})();
