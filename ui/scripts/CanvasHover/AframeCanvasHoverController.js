var canvasHoverController = (function () {

	var isInNavigation = false;

	function initialize(setupConfig) {
		application.transferConfigParams(setupConfig, controllerConfig);
		var cssLink = document.createElement("link");
		cssLink.type = "text/css";
		cssLink.rel = "stylesheet";
		cssLink.href = "scripts/CanvasHover/ho.css";
		document.getElementsByTagName("head")[0].appendChild(cssLink);
	}

	//config parameters
	var controllerConfig = {
		hoverColor: "darkred",
		showQualifiedName: false,
		showVersion: false,
		showIssues: false
	};

	function activate() {

		actionController.actions.mouse.hover.subscribe(handleOnMouseEnter);
		actionController.actions.mouse.unhover.subscribe(handleOnMouseLeave);

		createTooltipContainer();

		events.hovered.on.subscribe(onEntityHover);
		events.hovered.off.subscribe(onEntityUnhover);
	}

	function reset() {
		var hoveredEntities = events.hovered.getEntities();

		hoveredEntities.forEach(function (hoveredEntity) {
			var unHoverEvent = {
				sender: canvasHoverController,
				entities: [hoveredEntity]
			};

			events.hovered.off.publish(unHoverEvent);
		});
	}

	function createTooltipContainer() {

		var canvas = document.getElementById("canvas");

		var tooltipDivElement = document.createElement("DIV");
		tooltipDivElement.id = "tooltip";

		var namePElement = document.createElement("P");
		namePElement.id = "tooltipName";
		tooltipDivElement.appendChild(namePElement);

		if (controllerConfig.showQualifiedName) {
			var qualifiedNamePElement = document.createElement("P");
			qualifiedNamePElement.id = "tooltipQualifiedName";
			tooltipDivElement.appendChild(qualifiedNamePElement);
		}

		if (controllerConfig.showVersion) {
			var versionPElement = document.createElement("P");
			versionPElement.id = "tooltipVersion";
			tooltipDivElement.appendChild(versionPElement);
		}
		if (controllerConfig.showIssues) {
			var openIssuesPElement = document.createElement("P");
			openIssuesPElement.id = "tooltipOpenIssues";
			tooltipDivElement.appendChild((openIssuesPElement));

			var closedIssuesPElement = document.createElement("P");
			closedIssuesPElement.id = "tooltipClosedIssues";
			tooltipDivElement.appendChild((closedIssuesPElement));

			var openSecurityIssuesPElement = document.createElement("P");
			openSecurityIssuesPElement.id = "tooltipOpenSecurityIssues";
			tooltipDivElement.appendChild((openSecurityIssuesPElement));

			var closedSecurityIssuesPElement = document.createElement("P");
			closedSecurityIssuesPElement.id = "tooltipClosedSecurityIssues";
			tooltipDivElement.appendChild((closedSecurityIssuesPElement));
		}
		canvas.appendChild(tooltipDivElement);
	}

	function handleOnMousedown(canvasEvent) {
		isInNavigation = true;
	}

	function handleOnMouseup(canvasEvent) {
		isInNavigation = false;
	}

	function handleOnMouseEnter(eventObject) {
		if (isInNavigation) {
			return;
		}

		var entity = model.getEntityById(eventObject.target.id);
		if (entity === undefined) {
			return;
		}

		var applicationEvent = {
			sender: canvasHoverController,
			entities: [entity],
			posX: eventObject.layerX,
			posY: eventObject.layerY
		};
		events.hovered.on.publish(applicationEvent);
	}

	function handleOnMouseLeave(eventObject) {
		var entity = model.getEntityById(eventObject.target.id);
		if (entity === undefined) {
			return;
		}

		var applicationEvent = {
			sender: canvasHoverController,
			entities: [entity]
		};

		events.hovered.off.publish(applicationEvent);
	}

	function onEntityHover(applicationEvent) {
		var entity = applicationEvent.entities[0];

		if (entity === undefined) {
			events.log.error.publish({ text: "Entity is not defined" });
		}

		if (entity.isTransparent === true) {
			return;
		}

		let entityIsVisible = document.getElementById(entity.id).getAttribute('visible');
		if (!entityIsVisible) {
			return;
		}

		if (entity.type === "text") {
			return;
		}

		canvasManipulator.changeColorOfEntities([entity], controllerConfig.hoverColor, { name: "canvasHoverController" });

		$("#tooltipName").html(getTooltipName(entity));

		if (controllerConfig.showQualifiedName) {
			$("#tooltipQualifiedName").text(entity.qualifiedName);
		}
		if(controllerConfig.showVersion) {
			$("#tooltipVersion").text("Version: " + entity.version);
		}
		if(controllerConfig.showIssues) {
			let openIssuesSelector = $('#tooltipOpenIssues');
			let closedIssuesSelector = $('#tooltipClosedIssues');
			let openSecurityIssuesSelector = $('#tooltipOpenSecurityIssues');
			let closedSecurityIssuesSelector = $('#tooltipClosedSecurityIssues');
			if (entity.type === "Namespace") {
				openIssuesSelector.css("display", "none");
				closedIssuesSelector.css("display", "none");
				openSecurityIssuesSelector.css("display", "none");
				closedSecurityIssuesSelector.css("display", "none");
			} else {
				openIssuesSelector.text("Open Issues: " + entity.numberOfOpenIssues);
				closedIssuesSelector.text("Closed Issues: " + entity.numberOfClosedIssues);
				openSecurityIssuesSelector.text("Open Security Issues: " + entity.numberOfOpenSecurityIssues);
				closedSecurityIssuesSelector.text("Closed Security Issues: " + entity.numberOfClosedSecurityIssues);
				openIssuesSelector.css("display", "block");
				closedIssuesSelector.css("display", "block");
				openSecurityIssuesSelector.css("display", "block");
				closedSecurityIssuesSelector.css("display", "block");
			}
		}

		var tooltip = $("#tooltip");
		tooltip.css("top", applicationEvent.posY + 50 + "px");
		tooltip.css("left", applicationEvent.posX + 50 + "px");
		tooltip.css("display", "block");
	}

	function onEntityUnhover(applicationEvent) {
		var entity = applicationEvent.entities[0];

		canvasManipulator.resetColorOfEntities([entity], { name: "canvasHoverController" });

		$("#tooltip").css("display", "none");

	}

	function getTooltipName(entity) {

		if(entity.type === "Reference"){
			return "Reference: " + entity.name;
		}


		if (entity.type === "Namespace") {
			return "Package: " + entity.name;
		} else {
			var packages = entity.allParents.filter(parent => parent.type == "Namespace");

			if (packages.length == 0) {
				return entity.type + ": " + entity.name;
			}

			if (entity.type === "Method" && entity.signature != "") {
				return "Package: " + packages[0].name //namespace
					+ "<br/>" + entity.type + ": " + entity.signature;
			}
			return "Package: " + packages[0].name //namespace
				+ "<br/>" + entity.type + ": " + entity.name;
		}
	}

	return {
		initialize: initialize,
		activate: activate,
		reset: reset,
		handleOnMouseEnter: handleOnMouseEnter,
		handleOnMouseLeave: handleOnMouseLeave
    };
})();
