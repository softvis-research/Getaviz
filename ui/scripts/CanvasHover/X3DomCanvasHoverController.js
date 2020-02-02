var canvasHoverController = (function() {
    
    var isInNavigation = false;
	
    function initialize(setupConfig){
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
		showQualifiedName : false,
		showVersion : false,
		showIssues : false
	};
	
	function activate(){
		
		var multiPart = document.getElementById("multiPart");
		multiPart.addEventListener("mouseenter", handleOnMouseEnter, false);
		multiPart.addEventListener("mouseleave", handleOnMouseLeave, false);

		var canvas = document.getElementById("x3dom-x3dElement-canvas");
		canvas.addEventListener("mousedown", handleOnMousedown, false);
		canvas.addEventListener("mouseup", handleOnMouseup, false);
				
		createTooltipContainer();		
			
		events.hovered.on.subscribe(onEntityHover);
		events.hovered.off.subscribe(onEntityUnhover); 
	}
	
	function reset(){
		var hoveredEntities = events.hovered.getEntities();		
		
		hoveredEntities.forEach(function(hoveredEntity){
			var unHoverEvent = {					
					sender: canvasHoverController,
					entities: [hoveredEntity]
			};

			events.hovered.off.publish(unHoverEvent);	
		});		
	}
	
	function createTooltipContainer(){
		
		var canvas = document.getElementById("canvas");
		
		var tooltipDivElement = document.createElement("DIV");
		tooltipDivElement.id = "tooltip";
		
		var namePElement = document.createElement("P");
		namePElement.id = "tooltipName";
		tooltipDivElement.appendChild(namePElement);
		
		if(controllerConfig.showQualifiedName) {
			var qualifiedNamePElement = document.createElement("P");
			qualifiedNamePElement.id = "tooltipQualifiedName";
			tooltipDivElement.appendChild(qualifiedNamePElement);
		}
		
		if(controllerConfig.showVersion) {
			var versionPElement = document.createElement("P");
			versionPElement.id = "tooltipVersion";
			tooltipDivElement.appendChild(versionPElement);
		}
		if(controllerConfig.showIssues) {
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

	function handleOnMouseEnter(multipartEvent) {
		if(isInNavigation){
			return;
		}        

		var entity = model.getEntityById(multipartEvent.partID); 
		if(entity === undefined){
			entity = multipartEvent.target.id;
			console.log("entity: " + entity);
			events.log.error.publish({ text: "Entity of partID " + multipartEvent.partID + " not in model data."});
			return;
		}
		
		var applicationEvent = {
			sender		: canvasHoverController,
			entities	: [entity],
			posX		: multipartEvent.layerX,
			posY		: multipartEvent.layerY
		};
		
		events.hovered.on.publish(applicationEvent);		
	}

	function handleOnMouseLeave(multipartEvent) {
		
		var entity = model.getEntityById(multipartEvent.partID); 
		if(entity === undefined){
			events.log.error.publish({ text: "Entity of partID " + multipartEvent.partID + " not in model data."});
			return;
		}

		var applicationEvent = {			
			sender		: canvasHoverController,
			entities	: [entity]			
		};
		
		events.hovered.off.publish(applicationEvent);	
	}

	function onEntityHover(applicationEvent) {
		var entity = applicationEvent.entities[0];

        if(entity === undefined){
			events.log.error.publish({ text: "Entity is not defined"});
		}
		
		if(entity.isTransparent === true) {
			return;
		}

		if(entity.type === "text"){
			return;
		}

		if(entity.marked && entity.selected){
			canvasManipulator.unhighlightEntities([entity]);	
		} else {
			canvasManipulator.highlightEntities([entity], controllerConfig.hoverColor);	
		}
		        
		$("#tooltipName").text(getTooltipName(entity));
		if(controllerConfig.showQualifiedName) {
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
        tooltip.css("left", applicationEvent.posX + 50 +  "px");
		tooltip.css("display", "block");
    }
	
	function onEntityUnhover(applicationEvent) {
		var entity = applicationEvent.entities[0];
		
		if(entity.marked && entity.selected){
			canvasManipulator.highlightEntities([entity], controllerConfig.hoverColor);	
		} else {
			if(!entity.selected){
				canvasManipulator.unhighlightEntities([entity]);			
			}
			if(entity.type === "Namespace"){
			    canvasManipulator.unhighlightEntities([entity]);
            }
        }
		
		$("#tooltip").css("display", "none");

    }
	
	function getTooltipName(entity) {
        if(entity.type === "Method" || entity.type === "Function") {
			return entity.type + ": " + entity.signature;
        }
        
		if (entity.type === "Namespace") {
            return "Package: " + entity.name;
        }
		
		if(entity.type === "Variable"){
			return entity.type + ": " + entity.displayText;
		}
		
		if(entity.type === "TranslationUnit"){
			return "Translation unit: " + entity.name;
		}
        
        return entity.type + ": " + entity.name;        
    }

    return {
        initialize: initialize,
		activate: activate,
		reset: reset,
		handleOnMouseEnter: handleOnMouseEnter,
		handleOnMouseLeave: handleOnMouseLeave
    };    
})();
