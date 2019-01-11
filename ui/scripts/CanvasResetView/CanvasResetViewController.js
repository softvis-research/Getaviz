var canvasResetViewController = (function() {

	//config parameters	
	let controllerConfig = {
		button : true,
	};
	
	
	function initialize(setupConfig){	

		application.transferConfigParams(setupConfig, controllerConfig);		

		if(controllerConfig.button){		
			let cssLink = document.createElement("link");
			cssLink.type = "text/css";
			cssLink.rel = "stylesheet";
			cssLink.href = "scripts/CanvasResetView/rv.css";
			document.getElementsByTagName("head")[0].appendChild(cssLink);
		}
	}
	
	
	
	function activate(parent){        
		
		if(controllerConfig.button){	
			// style and position
			let taskDialogOkButton = document.createElement("INPUT");
			taskDialogOkButton.id = "resetViewButton";
			taskDialogOkButton.value = "Reset View";
			taskDialogOkButton.type = "button";		
			parent.appendChild(taskDialogOkButton);

            let $resetViewButton = $("#resetViewButton").jqxButton({ theme: "metro"});

			//$("#resetViewButton").jqxButton({ theme: "metro"});
            $resetViewButton.click(resetApplication);
		}
    }
	

	function reset(){
		canvasManipulator.reset();
	}
	
	
	function resetApplication() {  
		application.reset();
	}

		

    return {
        initialize	: initialize,
		reset		: reset,
		activate	: activate
    };    
})();