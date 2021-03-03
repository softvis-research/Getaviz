$(document).ready(function () {

	//parse setup if defined
	if (!window["setup"]) {
		console.log("No setup definition found!");
		return;
	}

	if (setup.loadPopUp) {
		var loadPopup = application.createPopup("Load Visualization", "Visualization is loading...", "RootLoadPopUp");
		document.body.appendChild(loadPopup);
		$("#RootLoadPopUp").jqxWindow({
			theme: "metro",
			width: 200,
			height: 200,
			isModal: true,
			autoOpen: true,
			resizable: false
		});
	}
	//load famix data
	$.getJSON(metaDataJsonUrl, initializeApplication);
});

function initializeApplication(metaDataJson) {
	//wait for canvas to be loaded full here...
	var canvas = document.getElementById(canvasId);
	if (!canvas && !lazyLoadingEnabled) {
		setTimeout(function () { initializeApplication(metaDataJson); }, 100);
		return;
	}

	model.initialize();

	var loaderPromise;
	//create entity model
	if (lazyLoadingEnabled) {
		neo4jModelLoadController.initialize();
		loaderPromise = neo4jModelLoadController.loadInitialData();
	} else {
		// this is synchronous, but if we wrap it into a promise we can use the same handling as for the async version
		loaderPromise = new Promise((resolve) => resolve(model.createEntititesFromMetadata(metaDataJson)));
	}

	loaderPromise.then(() => {
		actionController.initialize();
		canvasManipulator.initialize();

		//initialize application
		application.initialize();

		if (setup.loadPopUp) {
			$("#RootLoadPopUp").jqxWindow("close");
		}
	});
}

var application = (function () {

	var controllerFileFolder = "scripts/";

	var controllers = new Map();
	var activeControllers = new Map();

	var newActiveControllers = [];
	var oldActiveControllers = [];

	var uiConfigs = new Map();
	var currentUIConfig = null;

	var bodyElement;
	var canvasElement;


	//initialize application
	//*******************

	function initialize() {

		//parse setup if defined
		if (!window["setup"]) {
			events.log.error.publish({ text: "No setup configured" });
			return;
		}

		//first ui config is initial config

		if (setup.uis && setup.uis.length && setup.uis[0]) {
			setup.uis.forEach(function (uiConfig) {
				uiConfigs.set(uiConfig.name, uiConfig);
			});
			currentUIConfig = setup.uis[0];
		} else if (setup.ui) {
			currentUIConfig = setup.ui;
		}

		if (currentUIConfig === null) {
			events.log.error.publish({ text: "No UI config in setup found" });
			return;
		}

		//initialize controllers
		setup.controllers.forEach(function (controller) {
			loadAndInitializeController(controller);
		});

		//for ajax loading
		setTimeout(startConfigParsingAfterControllerLoading, 1);
	}


	function startConfigParsingAfterControllerLoading() {
		//check that all controllers loaded
		if (setup.controllers.length !== controllers.size) {
			console.debug("controllers not loaded yet...");
			setTimeout(startConfigParsingAfterControllerLoading, 1);
			return;
		}

		//get body and canvas elements
		bodyElement = document.body;
		canvasElement = document.getElementById("canvas");

		//create ui div element
		/*	AFRAME-WORKAROUND
		FÜR AFRAME - existierendes DIV statt neuem UI aus aframe.html
		id von "ui" zu "canvas" geändert
		var uiDIV = document.getElementById("canvas");*/

		var uiDIV = document.createElement("DIV");
		uiDIV.id = "ui";
		bodyElement.appendChild(uiDIV);
		currentUIConfig.uiDIV = uiDIV;

		//activate controller
		newActiveControllers = [];

		try {
			//parse ui config
			parseUIConfig(currentUIConfig.name, currentUIConfig, uiDIV);

			//activate controller
			activateController();
			events.log.info.publish({ text: "new config loaded: " + currentUIConfig.name });
		} catch (err) {
			events.log.error.publish({ text: err.message });
		}

		macroExplorerController.sendInitialEvent();
	}


	function resetApplication() {

		//controller reset
		activeControllers.forEach(function (controllerDiv, controllerObject, map) {
			if (controllerObject.reset) {
				controllerObject.reset();
			}
		});

		//model reset
		model.reset();

		//canvas reset
		canvasManipulator.reset();
	}


	function loadUIConfig(uiConfigName) {

		resetApplication();

		//same ui? -> nothing to do
		if (uiConfigName === currentUIConfig.name) {
			return;
		}

		var nextUIConfig = uiConfigs.get(uiConfigName);

		if (nextUIConfig === undefined) {
			events.log.error.publish({ text: "No UI config with name " + uiConfigName + " found" });
			return;
		}

		//create ui div element
		var uiDIV = document.createElement("DIV");
		uiDIV.id = "ui";
		bodyElement.appendChild(uiDIV);

		//remove current ui div
		var currentUIDIV = currentUIConfig.uiDIV;
		var currentUIDIVParent = currentUIDIV.parentElement;
		currentUIDIVParent.removeChild(currentUIDIV);

		//create new ui
		currentUIConfig = nextUIConfig;
		currentUIConfig.uiDIV = uiDIV;

		//collect old active controllers for deactivation
		oldActiveControllers = Array.from(activeControllers.keys());
		newActiveControllers = [];

		try {
			//parse new ui config
			parseUIConfig(currentUIConfig.name, currentUIConfig, uiDIV);

			//deactive controller
			deactivateController(oldActiveControllers);

			//activate controller
			activateController();

			events.log.info.publish({ text: "new config loaded: " + currentUIConfig.name });
		} catch (err) {
			events.log.error.publish({ text: err.message });
		}
	}


	//config parser
	//*******************

	function parseUIConfig(configName, configPart, parent) {

		//areas
		if (configPart.area !== undefined) {
			var area = configPart.area;
			if (area.orientation === undefined) {
				area.orientation = "vertical"
			}

			var splitterName = configName + "_" + area.name;
			var splitterId = "#" + splitterName;

			var splitterOrientation = area.orientation;

			var splitterResizable = true;
			if (area.resizable === false) {
				splitterResizable = area.resizable;
			}


			var firstPart = area.first;
			var secondPart = area.second;

			if (firstPart === undefined) {
				console.log("abc")
				console.log(area)
			}


			if (secondPart === undefined) {
				console.log("xyz")
				console.log(area)
			}


			//create jqwidget splitter
			var splitterObject = createSplitter(splitterName);

			//add splitter to parent
			parent.appendChild(splitterObject.splitter);

			var firstPanel = createPanel(firstPart);
			var secondPanel = createPanel(secondPart);

			$(splitterId).jqxSplitter({ theme: "metro", width: "100%", height: "100%", resizable: splitterResizable, orientation: splitterOrientation, panels: [firstPanel, secondPanel] });

			$(splitterId).on("resize", function(event) { canvasManipulator.resizeScene() });

			//pars area parts as config parts
			parseUIConfig(configName, firstPart, splitterObject.firstPanel);
			if (secondPart !== undefined) {
				parseUIConfig(configName, secondPart, splitterObject.secondPanel);
			}
		}

		//expander
		if (configPart.expanders !== undefined) {
			configPart.expanders.forEach(function (expander) {
				var expanderName = configName + "_" + expander.name;
				var expanderId = "#" + expanderName;
				var expanderTitle = expander.title;

				var expanderObject = createExpander(expanderName, expanderTitle);

				parent.appendChild(expanderObject.expander);

				$(expanderId).jqxExpander({ theme: "metro", width: "100%", height: "100%" });

				//pars expander parts as config parts
				var expanderContent = document.createElement("DIV");
				parseUIConfig(configName, expander, expanderContent);

				$(expanderId).jqxExpander('setContent', expanderContent);
			});
		}

		//canvas
		if (configPart.canvas !== undefined) {
			if (visMode != "aframe") {
				var canvasParentElement = canvasElement.parentElement;
				canvasParentElement.removeChild(canvasElement);

				parent.appendChild(canvasElement);
			} else {
				var canvasParentElement = canvasElement.parentElement;
				canvasParentElement.removeChild(canvasElement);

				parent.appendChild(canvasElement.cloneNode(true));
				//	evtl canvas löschen ??
			}
		}

		//controller
		if (configPart.controllers !== undefined) {
			configPart.controllers.forEach(function (controller) {
				setActivateController(controller, parent);
			});
		}

		//navigation
		if (configPart.navigation !== undefined) {
			createNavigationMode(configPart.navigation);
		}



	}


	//controller handling
	//*******************

	function loadAndInitializeController(controller) {
		var controllerName = controller.name;

		//controller allready loaded by html-file?
		if (window[controllerName]) {
			var controllerObject = window[controllerName];

			if (controllerObject.initialize) {
				controllerObject.initialize(controller);

				controllers.set(controllerName, controllerObject);
			}
		} else {
			events.log.error.publish({ text: "Controller " + controllerName + " not loaded!" });
		}
	}



	function setActivateController(controller, parent) {

		var controllerName = controller.name;
		var controllerObject = controllers.get(controllerName);

		if (controllerObject === undefined) {
			events.log.error.publish({ text: "controller " + controllerName + " undefined" });
			return;
		}

		if (activeControllers.has(controllerObject)) {

			let controllerDiv = activeControllers.get(controllerObject);

			let controllerDivParent = controllerDiv.parentElement;
			controllerDivParent.removeChild(controllerDiv);

			parent.appendChild(controllerDiv);

		} else {

			let controllerDiv = document.createElement("DIV");
			parent.appendChild(controllerDiv);

			activeControllers.set(controllerObject, controllerDiv);

			newActiveControllers.push(controllerObject);
		}

		//conroller also in new ui -> no deactivation
		if (oldActiveControllers.length !== 0) {
			let indexOfController = oldActiveControllers.indexOf(controllerObject);
			if (indexOfController !== -1) {
				oldActiveControllers.splice(indexOfController, 1);
			}
		}
	}

	function activateController() {
		newActiveControllers.forEach(function (controllerObject) {
			if (controllerObject.activate) {
				var controllerDiv = activeControllers.get(controllerObject);

				controllerObject.activate(controllerDiv);
			}
		});
	}


	function deactivateController(oldControllers) {

		oldControllers.forEach(function (controller) {
			if (!controller.deactivate) {
				return;
			}

			controller.deactivate();

			activeControllers.delete(controller);
		});

	}




	//gui creation
	//*******************

	function createNavigationMode(navigationObject) {
		if (visMode == "x3dom") {
			var navigationInfoElement = document.getElementById("navigationInfo");

			if (!navigationInfoElement) {
				var scene = document.getElementById("scene");

				navigationInfoElement = document.createElement("NAVIGATIONINFO");
				navigationInfoElement.id = "navigationInfo";

				scene.appendChild(navigationInfoElement);
			}

			if (navigationObject.type) {
				navigationInfoElement.setAttribute("type", navigationObject.type);
			}
			if (navigationObject.speed) {
				navigationInfoElement.setAttribute("speed", navigationObject.speed);
			}

			//Turntable seems not to work with 1.7 and dynamic adding
			if (navigationObject.typeParams) {
				navigationInfoElement.setAttribute("typeParams", navigationObject.typeParams);
			}
		}
		else console.debug("No x3dom - no navigationInfoElement");
	}

	function createPanel(areaPart) {
		var panel = {
			size: areaPart.size
		};
		if (areaPart.min !== undefined) {
			panel.min = areaPart.min;
		}
		if (areaPart.collapsible !== undefined) {
			panel.collapsible = areaPart.collapsible;
		}
		return panel;
	}


	function createSplitter(id) {
		var splitterDivElement = document.createElement("DIV");
		splitterDivElement.id = id;

		var firstPanelDivElement = document.createElement("DIV");
		firstPanelDivElement.id = id + "firstPanel";

		var secondPanelDivElement = document.createElement("DIV");
		secondPanelDivElement.id = id + "secondPanel";

		splitterDivElement.appendChild(firstPanelDivElement);
		splitterDivElement.appendChild(secondPanelDivElement);

		return {
			splitter: splitterDivElement,
			firstPanel: firstPanelDivElement,
			secondPanel: secondPanelDivElement
		};
	}

	function createExpander(id, title) {

		var expanderDivElement = document.createElement("DIV");
		expanderDivElement.id = id;

		var expanderHeadDivElement = document.createElement("DIV");
		expanderHeadDivElement.innerHTML = title;
		expanderDivElement.appendChild(expanderHeadDivElement);

		var expanderContentDivElement = document.createElement("DIV");
		expanderDivElement.appendChild(expanderContentDivElement);

		return {
			expander: expanderDivElement,
			head: expanderHeadDivElement,
			content: expanderContentDivElement
		};
	}


	function createPopup(title, text, popupId, okButtonId) {

		var popupWindowDiv = document.createElement("DIV");
		popupWindowDiv.id = popupId;

		var popupTitleDiv = document.createElement("DIV");
		popupWindowDiv.appendChild(popupTitleDiv);
		popupTitleDiv.innerHTML = title;

		var popupContentDiv = document.createElement("DIV");
		popupWindowDiv.appendChild(popupContentDiv);

		//Text
		var popupText = document.createElement("DIV");
		popupContentDiv.appendChild(popupText);
		popupText.innerHTML = text;

		return popupWindowDiv;
	}


	//Helper

	function transferConfigParams(setupConfig, controllerConfig) {
		for (var property in setupConfig) {
			if (property === "name") {
				continue;
			}

			if (setupConfig.hasOwnProperty(property) && controllerConfig.hasOwnProperty(property)) {
				controllerConfig[property] = setupConfig[property];
			}

			if (setupConfig.hasOwnProperty(property) && !controllerConfig.hasOwnProperty(property)) {
				events.log.warning.publish({ text: "setup property: " + property + " not in controller config" });
			}
		}

	}


	return {
		initialize: initialize,
		loadUIConfig: loadUIConfig,
		transferConfigParams: transferConfigParams,
		createPopup: createPopup,
		reset: resetApplication
	};

})();




