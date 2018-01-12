var canvasHoverController = (function() {
    
	var isInNavigation = false;    
	
    function initialize(){ 	

		var cssLink = document.createElement("link");
		cssLink.type = "text/css";
		cssLink.rel = "stylesheet";
		cssLink.href = "scripts/CanvasHover/ho.css";
		document.getElementsByTagName("head")[0].appendChild(cssLink);
        		
    }
	
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
				}	

				events.hovered.off.publish(unHoverEvent);	
		});		
	}
	
	
	
	function createTooltipContainer(){
		
		var canvas = document.getElementById("canvas");
		
		var tooltipDivElement = document.createElement("DIV");
		tooltipDivElement.id = "tooltip";
		
		var namePElement = document.createElement("P");
		namePElement.id = "tooltipName";
		
		var qualifiedNamePElement = document.createElement("P");
		qualifiedNamePElement.id = "tooltipQualifiedName";
		
		tooltipDivElement.appendChild(namePElement);
		tooltipDivElement.appendChild(qualifiedNamePElement);
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
		if(entity == undefined){
			events.log.error.publish({ text: "Entity of partID " + multipartEvent.partID + " not in model data."});
			return;
		}
		
		var applicationEvent = {
			sender		: canvasHoverController,
			entities	: [entity],
			posX		: multipartEvent.layerX,
			posY		: multipartEvent.layerY
		}	
		
		events.hovered.on.publish(applicationEvent);		
	}

	function handleOnMouseLeave(multipartEvent) {
		
		var entity = model.getEntityById(multipartEvent.partID); 
		if(entity == undefined){
			events.log.error.publish({ text: "Entity of partID " + multipartEvent.partID + " not in model data."});
			return;
		}

		var applicationEvent = {			
			sender		: canvasHoverController,
			entities	: [entity]			
		}	
		
		events.hovered.off.publish(applicationEvent);	
	}
	



	
	
	function onEntityHover(applicationEvent) {
		var entity = applicationEvent.entities[0];
		
		if(entity == undefined){
			events.log.error.publish({ text: "Entity is not defined"});
		}

		if(entity.marked && entity.selected){
			canvasManipulator.unhighlightEntities([entity]);	
		} else {
			canvasManipulator.highlightEntities([entity], canvasManipulator.colors.darkred);	
		}
		        
		$("#tooltipName").text(getTooltipName(entity));
		$("#tooltipQualifiedName").text(entity.qualifiedName);
		
		var tooltip = $("#tooltip");
        tooltip.css("top", applicationEvent.posY + 50 + "px");
        tooltip.css("left", applicationEvent.posX + 50 +  "px");		
		tooltip.css("display", "block");
	}
	
	function onEntityUnhover(applicationEvent) {
		var entity = applicationEvent.entities[0];
		
		if(entity.marked && entity.selected){
			canvasManipulator.highlightEntities([entity], canvasManipulator.colors.darkred);	
		} else {
			if(!entity.selected){
				canvasManipulator.unhighlightEntities([entity]);			
			}
        }
		
		$("#tooltip").css("display", "none");		
	}
	
	
	function getTooltipName(entity) {
        
        if(entity.type == "Method") {
			return entity.type + ": " + entity.signature;
        }
        
		if (entity.type == "Namespace") {
            return "Package: " + entity.name;
        }
        
        return entity.type + ": " + entity.name;        
    }
	

    return {
        initialize: initialize,
		activate: activate,
		reset: reset
    };    
})();