var metricAnimationController = (function() {

    var controllerConfig = {
        bla : true
    };

	function initialize(setupConfig){
        //console.debug("initialize metric Animation Controller");
        application.transferConfigParams(setupConfig, controllerConfig);
    }

	function activate(rootDiv){

        //console.debug("activate metric Animation Controller");

		// let metricAnimationDiv = document.createElement("DIV");
        // metricAnimationDiv.id = "metricAnimationDiv";
        //
		// let capture = document.createElement("H1");
		// let captureText = document.createTextNode("Metric Animations");
        // metricAnimationDiv.appendChild(capture);
		// capture.appendChild(captureText);

        // rootDiv.appendChild(metricAnimationDiv);
    }

	function reset(){
        //console.debug("reset metric Animation Controller");
	}

    function deactivate(){
        reset();
    }

    return {
        initialize: initialize,
		activate: activate,
        deactivate:	deactivate,
        reset: reset
    };
})();
