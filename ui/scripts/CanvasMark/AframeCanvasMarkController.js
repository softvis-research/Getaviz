var canvasMarkController = (function() {
    
	 
	var SELECTION_MODES = {
		UP: "UP",
		DOWN: "DOWN",
		DURATION: "DURATION"
	};
	
	//config parameters	
	var controllerConfig = {		
		setCenterOfRotation : false,
        markingColor: "orange",
    	selectionMouseKey: 1,
		selectionMode: "UP",
		selectionDurationSeconds: 0.5,
		selectionMoveAllowed: false,
		showProgressBar: false
	};

	var downActionEventObject;
    
	function initialize(setupConfig){	

		application.transferConfigParams(setupConfig, controllerConfig);	
			
    }	
				
	function activate(){

		actionController.actions.mouse.key[controllerConfig.selectionMouseKey].down.subscribe(downAction);
		actionController.actions.mouse.key[controllerConfig.selectionMouseKey].up.subscribe(upAction);
		actionController.actions.mouse.key[controllerConfig.selectionMouseKey].during.subscribe(duringAction);
		actionController.actions.mouse.move.subscribe(mouseMove);	

		events.marked.on.subscribe(onEntityMarked);
		events.marked.off.subscribe(onEntityUnmarked); 
    }
		
	function reset(){
		var markedEntities = events.marked.getEntities();		
		
		canvasManipulator.resetColorOfEntities(markedEntities);	
	}




	function downAction(eventObject, timestamp){

		downActionEventObject = eventObject;

		if(!eventObject.entity){
			return;
		}

		if(controllerConfig.selectionMode === "DOWN"){
			handleOnClick(eventObject);
			return;
		}
		
		downActionEventObject = eventObject;

		//	"progressBar.jqxProgressBar is not a function"
		/*if(controllerConfig.selectionMode === "DURATION" && controllerConfig.showProgressBar){
			showProgressBar(eventObject);
		}*/
	}

	function upAction(eventObject){

		if(!downActionEventObject){
			return;
		}

		if(controllerConfig.selectionMode === "UP"){
			handleOnClick(downActionEventObject);
			return;
		}

		/*if(controllerConfig.selectionMode === "DURATION" && controllerConfig.showProgressBar){
			hideProgressBar();
		}*/
	}

	function duringAction(eventObject, timestamp, timeSinceStart){

		if(!downActionEventObject){
			return;
		}

		if(controllerConfig.selectionMode !== "DURATION"){
			return;
		}

		if(timeSinceStart > ( 1000 * controllerConfig.selectionDurationSeconds)){
			//hideProgressBar();
			handleOnClick(downActionEventObject);
			downActionEventObject = null;
		}
	}

	function mouseMove(eventObject, timestamp){
		/*if(!downActionEventObject){
			return;
		}

		if(!controllerConfig.selectionMoveAllowed){
			hideProgressBar();
			downActionEventObject = null;
		}*/
	}


	function handleOnClick(eventObject) {
		if(eventObject.entity != null) {
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
            /*if(controllerConfig.setCenterOfRotation){
                canvasManipulator.setCenterOfRotation(eventObject.entity);
            }*/
        }
	}





	function onEntityMarked(applicationEvent) {
		var entity = applicationEvent.entities[0];
		if(entity == undefined) {
			console.debug("no entity");
			return;
		}
		
		if(entity.hovered){
			console.debug("Entity hovered");
			canvasManipulator.unhighlightEntities([entity]);			
		}
		canvasManipulator.changeColorOfEntities([entity], controllerConfig.markingColor);
	}

	function onEntityUnmarked(applicationEvent) {
		var entity = applicationEvent.entities[0];
		canvasManipulator.resetColorOfEntities([entity]);	
	}



	function showProgressBar(eventObject){
		
		var canvas = document.getElementById("canvas");
		
		var progressBarDivElement = document.createElement("DIV");
		progressBarDivElement.id = "progressBarDiv";
		
		canvas.appendChild(progressBarDivElement);

		var progressBar = $("#progressBarDiv");

		progressBar.jqxProgressBar({ 
			width: 				250, 
			height: 			30, 
			value: 				100, 
			animationDuration: 	controllerConfig.selectionDurationSeconds * 1000, 
			template: 			"success"
		});


		progressBar.css("top", eventObject.layerY + 10 + "px");
        progressBar.css("left", eventObject.layerX + 10 +  "px");

		progressBar.css("z-index", "1");
		progressBar.css("position", "absolute");

		progressBar.css("width", "250px");	
		progressBar.css("height", "30px");	

		progressBar.css("display", "block");

	}

	function hideProgressBar(){		
		
		var progressBarDivElement = document.getElementById("progressBarDiv");

		if(!progressBarDivElement){
			return;
		}	

		var canvas = document.getElementById("canvas");		
		canvas.removeChild(progressBarDivElement);
	}
		

    return {
        initialize			: initialize,
		reset				: reset,
		activate			: activate,
		onEntityMarked		: onEntityMarked,
		onEntityUnmarked	: onEntityUnmarked,
		SELECTION_MODES		: SELECTION_MODES
    };    
})();