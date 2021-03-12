var canvasSelectController = (function () {

	var SELECTION_MODES = {
		UP: "UP",
		DOWN: "DOWN",
		DURATION: "DURATION"
	};

	//config parameters
	var controllerConfig = {
		setCenterOfRotation: false,
		color: "darkred",
		multiselectColor: "240 128 128", //lightcoral - just for test purposes
		selectionMouseKey: 1,
		selectionMode: SELECTION_MODES.UP,
		selectionDurationSeconds: 0.5,
		selectionMoveAllowed: false,
		showProgressBar: false,

		useMultiselect: true
	};

	var selectedEntities = [];

	var downActionEventObject;


	function initialize(setupConfig) {
		application.transferConfigParams(setupConfig, controllerConfig);
	}

	function activate() {

		actionController.actions.mouse.key[controllerConfig.selectionMouseKey].down.subscribe(downAction);
		actionController.actions.mouse.key[controllerConfig.selectionMouseKey].up.subscribe(upAction);
		actionController.actions.mouse.key[controllerConfig.selectionMouseKey].during.subscribe(duringAction);
		actionController.actions.mouse.move.subscribe(mouseMove);

		events.selected.on.subscribe(onEntitySelected);
		events.selected.off.subscribe(onEntityUnselected);
		events.componentSelected.on.subscribe(onComponentSelected);
		events.antipattern.on.subscribe(onComponentSelected);
	}

	function onComponentSelected(applicationEvent) {
		console.log("executed")
		var selectedEntities = events.selected.getEntities();
		selectedEntities.forEach(function (selectedEntity) {

			var unselectEvent = {
				sender: canvasSelectController,
				entities: [selectedEntity]
			}

			events.selected.off.publish(unselectEvent);
		});
	}

	function reset() {
		var unselectEvent = {
			sender: canvasSelectController,
			entities: selectedEntities
		};

		events.selected.off.publish(unselectEvent);
	}

	function downAction(eventObject, timestamp) {

		downActionEventObject = null;

		if (!eventObject.entity) {
			return;
		}

		if (controllerConfig.selectionMode == "DOWN") {
			handleOnClick(eventObject);
			return;
		}

		downActionEventObject = eventObject;

		if (controllerConfig.selectionMode == "DURATION" && controllerConfig.showProgressBar) {
			showProgressBar(eventObject);
		}
	}

	function upAction(eventObject) {

		if (!downActionEventObject) {
			return;
		}

		if (controllerConfig.selectionMode == "UP") {
			handleOnClick(downActionEventObject);
			return;
		}

		if (controllerConfig.selectionMode == "DURATION" && controllerConfig.showProgressBar) {
			hideProgressBar();
		}
	}

	function duringAction(eventObject, timestamp, timeSinceStart) {

		if (!downActionEventObject) {
			return;
		}

		if (controllerConfig.selectionMode != "DURATION") {
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

		const alreadySelected = eventObject.entity === selectedEntities[0];

		//always deselect the previously selected entities
		if (selectedEntities.length != 0) {
			const unselectEvent = {
				sender: canvasSelectController,
				entities: selectedEntities
			}

			events.selected.off.publish(unselectEvent);
		};

		//select the clicked entities only if the clicked entities are not already selected
		//otherwise the clicked entities should only be deselected
		if (!alreadySelected) {
			let newSelectedEntities = [eventObject.entity];

			if (controllerConfig.useMultiselect) {
				const visibleChildren = model.getAllChildrenOfEntity(eventObject.entity).filter(entity => !entity.filtered);
				newSelectedEntities = newSelectedEntities.concat(visibleChildren);
			}

			const applicationEvent = {
				sender: canvasSelectController,
				entities: newSelectedEntities
			};
			events.selected.on.publish(applicationEvent);
		}
	}

	function onEntitySelected(applicationEvent) {

		var selectedEntity = applicationEvent.entities[0];
		selectedEntities = applicationEvent.entities;

		if (selectedEntity.type == "text") {
			return;
		}


		//highlight multiselected entities with specific color
		canvasManipulator.changeColorOfEntities(selectedEntities.slice(1), controllerConfig.multiselectColor, { name: "canvasSelectController" });
		//higlight selected entity with regular color
		canvasManipulator.changeColorOfEntities([selectedEntity], controllerConfig.color, { name: "canvasSelectController" });

		//center of rotation
		if (controllerConfig.setCenterOfRotation) {
			canvasManipulator.setCenterOfRotation(selectedEntity);
		}
	}

	function onEntityUnselected(applicationEvent) {
		canvasManipulator.resetColorOfEntities(applicationEvent.entities, { name: "canvasSelectController" });
		selectedEntities = new Array();
	}

	function showProgressBar(eventObject) {

		var canvas = document.getElementById("canvas");

		var progressBarDivElement = document.createElement("DIV");
		progressBarDivElement.id = "progressBarDiv";

		canvas.appendChild(progressBarDivElement);

		var progressBar = $("#progressBarDiv");

		progressBar.jqxProgressBar({
			width: 250,
			height: 30,
			value: 100,
			animationDuration: controllerConfig.selectionDurationSeconds * 1000,
			template: "danger"
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

		var progressBarDivElement = document.getElementById("progressBarDiv");

		if (!progressBarDivElement) {
			return;
		}

		var canvas = document.getElementById("canvas");
		canvas.removeChild(progressBarDivElement);
	}


	return {
		initialize: initialize,
		reset: reset,
		activate: activate,
		SELECTION_MODES: SELECTION_MODES
	};
})();

