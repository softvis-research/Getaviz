var canvasResetViewController = (function() {

	//config parameters	
	var controllerConfig = {
		button : true,
	}
	
	
	function initialize(setupConfig){	

		application.transferConfigParams(setupConfig, controllerConfig);		

		if(controllerConfig.button){		
			var cssLink = document.createElement("link");
			cssLink.type = "text/css";
			cssLink.rel = "stylesheet";
			cssLink.href = "scripts/CanvasResetView/rv.css";
			document.getElementsByTagName("head")[0].appendChild(cssLink);
		}
	}
	
	
	
	function activate(parent){        
		
		if(controllerConfig.button){	
			// style and position
			var taskDialogOkButton = document.createElement("INPUT");
			taskDialogOkButton.id = "resetViewButton";
			taskDialogOkButton.value = "Reset View";
			taskDialogOkButton.type = "button";		
			parent.appendChild(taskDialogOkButton);	
			
			$("#resetViewButton").jqxButton({ theme: "metro"});
			$("#resetViewButton").click(resetApplication);
		}
    }
	
	
	function reset(){
		document.getElementById("x3dElement").runtime.showAll("negZ");
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