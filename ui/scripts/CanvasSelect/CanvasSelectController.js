var canvasSelectController = (function() {
	
	var SELECTION_MODES = {
		UP 			: "UP",
		DOWN 		: "DOWN",
		DURATION	: "DURATION"
	};

	//config parameters	
	var controllerConfig = {
		setCenterOfRotation : false,
        color: "darkred",
		selectionMouseKey: 1,
		selectionMode: SELECTION_MODES.UP,					
		selectionDurationSeconds: 0.5,
		selectionMoveAllowed: false,
		showProgressBar: false,
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
			
		events.selected.on.subscribe(onEntitySelected);
		events.selected.off.subscribe(onEntityUnselected);
        events.componentSelected.on.subscribe(onComponentSelected);
        events.antipattern.on.subscribe(onComponentSelected);
	}

	function onComponentSelected(applicationEvent){
		console.log("executed")
        var selectedEntities = events.selected.getEntities();
        selectedEntities.forEach(function(selectedEntity){

            var unselectEvent = {
                sender: canvasSelectController,
                entities: [selectedEntity]
            }

            events.selected.off.publish(unselectEvent);
        });
	}
	
	function reset(){
		var selectedEntities = events.selected.getEntities();		
		
		selectedEntities.forEach(function(selectedEntity){
			var unselectEvent = {
                sender: canvasSelectController,
                entities: [selectedEntity]
            };

			events.selected.off.publish(unselectEvent);	
		});		
	}

	function downAction(eventObject, timestamp){

		downActionEventObject = null;

		if(!eventObject.entity){
			return;
		}

		if(controllerConfig.selectionMode == "DOWN"){
			handleOnClick(eventObject);
			return;
		}
		
		downActionEventObject = eventObject;

		if(controllerConfig.selectionMode == "DURATION" && controllerConfig.showProgressBar){
			showProgressBar(eventObject);
		}
	}

	function upAction(eventObject){

		if(!downActionEventObject){
			return;
		}

		if(controllerConfig.selectionMode == "UP"){
			handleOnClick(downActionEventObject);
			return;
		}

		if(controllerConfig.selectionMode == "DURATION" && controllerConfig.showProgressBar){
			hideProgressBar();
		}
	}

	function duringAction(eventObject, timestamp, timeSinceStart){

		if(!downActionEventObject){
			return;
		}

		if(controllerConfig.selectionMode != "DURATION"){
			return;
		}

		if(timeSinceStart > ( 1000 * controllerConfig.selectionDurationSeconds)){
			hideProgressBar();
			handleOnClick(downActionEventObject);
			downActionEventObject = null;			
			return;
		}
	}

	function mouseMove(eventObject, timestamp){
		if(!downActionEventObject){
			return;
		}

		if(!controllerConfig.selectionMoveAllowed){
			hideProgressBar();
			downActionEventObject = null;
		}
	}

	function handleOnClick(eventObject) {
		
		var selectedEntities = [];
		selectedEntities.push(eventObject.entity);

		selectedEntities = selectedEntities.concat(model.getAllChildrenOfEntity(eventObject.entity));
				
		var applicationEvent = {			
			sender: canvasSelectController,
			entities: selectedEntities
		};
		
		events.selected.on.publish(applicationEvent);		
	}
	
	function onEntitySelected(applicationEvent) {	
		
		var entity = applicationEvent.entities[0];	
		
		var selectedEntities = events.selected.getEntities();		
		
		//select same entity again -> nothing to do
		if(selectedEntities.has(entity)){
			return;
		}

        if(entity.type == "text"){
            return;
        }

        if(entity.type == "Namespace"){
		    return;
        }

		//unhighlight old selected entities	for single select	
		if(selectedEntities.size != 0){
		
			selectedEntities.forEach(function(selectedEntity){
								
				var unselectEvent = {					
					sender: canvasSelectController,
					entities: [selectedEntity]
				}	

				events.selected.off.publish(unselectEvent);	
			});
		}
		
		//higlight new selected entity
		canvasManipulator.highlightEntities([entity], controllerConfig.color);		

		//center of rotation
		if(controllerConfig.setCenterOfRotation){
			canvasManipulator.setCenterOfRotation(entity);
		}
    }
	
	function onEntityUnselected(applicationEvent){
		var entity = applicationEvent.entities[0];
		canvasManipulator.unhighlightEntities([entity]);		
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
			template: 			"danger"
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
		SELECTION_MODES		: SELECTION_MODES
    };    
})();

