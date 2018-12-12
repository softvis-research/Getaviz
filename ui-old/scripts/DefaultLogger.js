var defaultLogger = (function() {

	var controllerConfig = {
		logInfoConsole : false,
		logWarningConsole : true,
		logErrorConsole : true,
		logActionConsole : false,
		logEventConsole : false,
		logManipulationConsole : false,
		logQueueSize : 1000
	}

	var infoLogQueue = new Array(); 
	var warningLogQueue = new Array(); 
	var errorLogQueue = new Array(); 
	var actionLogQueue = new Array(); 
	var eventLogQueue = new Array(); 
	var manipulationLogQueue = new Array(); 

	function initialize(setupConfig){ 	
		application.transferConfigParams(setupConfig, controllerConfig);
    }	
	
	function activate(){
		events.log.info.subscribe(logInfo);
		events.log.warning.subscribe(logWarning);
		events.log.error.subscribe(logError);
		events.log.action.subscribe(logAction);
		events.log.event.subscribe(logEvent);
		events.log.manipulation.subscribe(logManipulation);
	}
	
	function logInfo(logObject){		
		infoLogQueue.push({ 
			text: "INFO: " + logObject.text, 
			logObject: logObject 
		});
		if(infoLogQueue.length > controllerConfig.logQueueSize){
			infoLogQueue.shift();
		}

		if(controllerConfig.logInfoConsole){
			console.log("INFO: " + logObject.text );
		}		
	}

	function logWarning(logObject){			
		warningLogQueue.push({ 
			text: "WARNING: " + logObject.text, 
			logObject: logObject 
		});
		if(warningLogQueue.length > controllerConfig.logQueueSize){
			warningLogQueue.shift();
		}

		if(controllerConfig.logWarningConsole){			
			console.log("WARNING: " + logObject.text );		
		}
	}

	function logError(logObject){					
		errorLogQueue.push({ 
			text: "ERROR: " + logObject.text, 
			logObject: logObject 
		});
		if(errorLogQueue.length > controllerConfig.logQueueSize){
			errorLogQueue.shift();
		}

		if(controllerConfig.logErrorConsole){	
			console.log("ERROR: " + logObject.text );
		}		
	}

	function logAction(logObject){		
		actionLogQueue.push({ 
			text: "ACTION: " + logObject.actionObject.type + " " + logObject.eventObject.which, 
			logObject: logObject 
		});
		if(actionLogQueue.length > controllerConfig.logQueueSize){
			actionLogQueue.shift();
		}

		if(controllerConfig.logActionConsole){				
			console.log("ACTION: " + logObject.actionObject.type + " " + logObject.eventObject.which );		
		}
	}

	function logEvent(logObject){		
		eventLogQueue.push({ 
			text: "EVENT: " + logObject.eventTypeName, 
			logObject: logObject 
		});
		if(eventLogQueue.length > controllerConfig.logQueueSize){
			eventLogQueue.shift();
		}

		if(controllerConfig.logEventConsole){				
			console.log("EVENT: " + logObject.eventTypeName);		
		}
	}

	function logManipulation(logObject){		
		manipulationLogQueue.push({ 
			text: "MANIPULATION: " + logObject.manipulation.manipulationFunction, 
			logObject: logObject 
		});
		if(manipulationLogQueue.length > controllerConfig.logQueueSize){
			manipulationLogQueue.shift();
		}

		if(controllerConfig.logManipulationConsole){				
			console.log("MANIPULATION: " + logObject.manipulation.manipulationFunction );		
		}
	}






	function getLogQueues(){
		return {
			infoLogQueue 			: infoLogQueue,
			warningLogQueue 		: warningLogQueue,
			errorLogQueue 			: errorLogQueue,
			actionLogQueue 			: actionLogQueue,
			eventLogQueue 			: eventLogQueue,
			manipulationLogQueue 	: manipulationLogQueue
		}
	}

	return {
		initialize			: initialize,
		activate			: activate,
		getLogQueues 		: getLogQueues	
	};    
})();