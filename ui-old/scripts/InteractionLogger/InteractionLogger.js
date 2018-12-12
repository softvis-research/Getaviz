var interactionLogger = (function() {
	
	var controllerConfig = {
		serverURL : "http://localhost/interactionlogger/index.php",
		logOnConsole : false,
	 	logOnServer : true,
	}
	
	
	
	
	//*********************************************************************************************************************	
	//Initialize
	//*********************************************************************************************************************	
				
	var initialTime;	
	var viewArea;	
	var eventTrigger;
	var actionTrigger;
	
	var logStrings = [];
	var logCounter = 1;
	


	function initialize(setupConfig){ 	
		application.transferConfigParams(setupConfig, controllerConfig);
  			
		var x3domRuntime = document.getElementById('x3dElement').runtime;
		viewArea = x3domRuntime.canvas.doc._viewarea;			
	}

	function activate(){

		initialTime = Date.now();	

		var logString =  
			"TIMESTAMP;" +
			"TIME;" +
			"ACTION/EVENT/MANIPULATION;" +
			"TYPE;" +
			"KEY;" +
			"TARGET-ID;" +
			"TARGET-X;" +
			"TARGET-Y;" +
			"TARGET-Z;" +
			"DURATION;" +
			"MOUSEDISTANCE-X;" +
			"MOUSEDISTANCE-Y;" +
			"VIEWPOS-X;" +
			"VIEWPOS-Y;" +
			"VIEWPOS-Z;" +
			"EVENT-TRIGGER;" +
			"ACTION-TRIGGER;"			
		
		if(controllerConfig.logOnServer){
			logServer(logString);
		}
		
		if(controllerConfig.logOnConsole){
			logConsole(logString);
		}
					
		events.log.action.subscribe(logAction);
		events.log.event.subscribe(logEvent);
		events.log.manipulation.subscribe(logManipulation);

		events.log.controller.subscribe(logController);

		events.log.info.subscribe(logInfo);
		events.log.warning.subscribe(logWarning);
		events.log.error.subscribe(logError);
	}


	
	//*********************************************************************************************************************	
	//Log functions
	//*********************************************************************************************************************	

	
	function logConsole(logString){
		console.log("INTERACTIONLOGGER;" + logString);			
	}
	
	function logServer(logString){
		
		logStrings.push(logString);
		
		if(logStrings.length > 50){
			logServerFlush();
		}
	}
	
	function logServerFlush(){
		var post = 	"logFile=" + initialTime + ".csv" + "&" +
					"logText=";
					
		for(var i = 0; i < logStrings.length; i++) {			
			
			//Erste Zeile Uhrzeit ergänzen
			if(logCounter == 1){
				
				var timeStemp = new Date();
				
				var year = timeStemp.getFullYear();
				var month = timeStemp.getMonth() + 1;
				var day = timeStemp.getDate();
								
				var seconds = timeStemp.getSeconds();
				var minutes = timeStemp.getMinutes();
				var hours  	= timeStemp.getHours();
				
				var timeString = day + "." + month + "." +year + " " + hours + ":" + minutes + ":" + seconds;
				
				post = post + timeString + ";" + logStrings[i] + "\n";
			} else {
				post = post + logCounter + ";" + logStrings[i] + "\n";
			}
			
			logCounter++;
		}		
		
		var xmlHttp = new XMLHttpRequest();
		xmlHttp.open("POST", controllerConfig.serverURL, true);
		xmlHttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
		xmlHttp.send(post);
		
		logStrings = [];
	}
	
	
	function log(aemt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY){
						
		//time
		var timestamp = Date.now();
		var time = timestamp - initialTime;
		
		var millis  = time;
		var seconds = Math.floor(time / 1000);
		var minutes = Math.floor(time / 60000);
		var hours  	= Math.floor(time / 3600000);
		
		var timeString = hours + ":" + (minutes % 60) + ":" + (seconds % 60) + ":" + (millis % 1000);
		
		//viewPosition
		var viewMatrix = viewArea.getViewMatrix();
		var viewPosition = viewMatrix._03.toString().replace(".", ",") + ";" + viewMatrix._13.toString().replace(".", ",") + ";" + viewMatrix._23.toString().replace(".", ",");
		
		var logString = timestamp +
			";" + timeString + 
			";" + aemt + 
			";" + type + 
			";" + key + 
			";" + targetID + 
			";" + targetX + 
			";" + targetY + 
			";" + targetZ + 
			";" + duration + 
			";" + mDistanceX + 
			";" + mDistanceY +
			";" + viewPosition
				
		if(controllerConfig.logOnServer){
			logServer(logString);
		}
		
		if(controllerConfig.logOnConsole){
			logConsole(logString);
		}	
	}
	

	var lastActionLogObject = null;

	function logAction(logObject){		

		if(lastActionLogObject !== null && 
			lastActionLogObject.actionObject.type === logObject.actionObject.type &&
			lastActionLogObject.eventObject.which === logObject.eventObject.which &&
			lastActionLogObject.eventObject.partID === logObject.eventObject.partID			
			){
			return;
		}

		//log(aemt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY)
		log("ACTION", logObject.actionObject.type, logObject.eventObject.which, logObject.eventObject.partID, "", "", "", "", "", "");		

		lastActionLogObject = logObject;
	}

	function logEvent(logObject){		
	
		//log(aemt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY)
		log("EVENT", logObject.eventTypeName, "", "", "", "", "", "", "", "");		

	}

	function logManipulation(logObject){		
		
		//log(aemt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY)
		log("MANIPULATION", logObject.manipulation.manipulationFunction, "", "", "", "", "", "", "", "");

	}

	function logController(logObject){	

		//log(aemt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY)
		log("CONTROLLER", logObject.text, logObject.var1, logObject.var2, logObject.var3, logObject.var4, "", "", "", "");
	}




	function logInfo(logObject){		

		//log(aemt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY)
		log("INFO", logObject.text, "", "", "", "", "", "", "", "");
	}

	function logWarning(logObject){		

		//log(aemt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY)
		log("WARNING", logObject.text, "", "", "", "", "", "", "", "");
	}

	function logError(logObject){	

		//log(aemt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY)
		log("ERROR", logObject.text, "", "", "", "", "", "", "", "");
	}


	//******************
	// Helper
	//******************

	


	/*
	
	//*********************************************************************************************************************		
	//NAVIGATION	
	//*********************************************************************************************************************		
	
	var mouseLeft 		= false;
	var mouseMiddle 	= false;
	var mouseRight 		= false;	
	
	var rotate			= false;
	var zoom			= false;
	var pan				= false;
	 
	
	
	function navigationMouseDown(event){
		switch(event.which) {
			case 1:  mouseLeft = true; break;  //left
			case 2:  mouseMiddle = true; break;  //middle
			case 3:  mouseRight = true; break;  //right
		}
	}
	
	function navigationMouseMove(event){
		if(mouseLeft){
			rotate 	= true;					
		} else if(mouseMiddle){			
			pan 	= true;
		} else if(mouseRight){
			zoom	= true;
		}
	}
		
	function navigationMouseUp(event){	
	
		if(rotate){			
			logEvent("EXPLORE", "Rotate");
			logManipulation("Viewpoint", "Changed", "");					
		}
		if(zoom){			
			logEvent("EXPLORE", "Zoom");
			logManipulation("Viewpoint", "Changed", "");			
		}		
		if(pan){			
			logEvent("EXPLORE", "Pan");
			logManipulation("Viewpoint", "Changed", "");
		}
		
		mouseLeft 		= false;
		mouseMiddle 	= false;
		mouseRight 		= false;
		
		rotate 	= false;
		pan 	= false;
		zoom	= false;
	}
	
		
	function navaigationMouseDbClick(event){	
		logEvent("EXPLORE", "Focus");
		logManipulation("Viewpoint", "Changed", "");
	}
	
	function navigationMouseWheel(event){	
		logEvent("EXPLORE", "Zoom");
		logManipulation("Viewpoint", "Changed", "");
	}
	
	
	
	//*********************************************************************************************************************		
	//MOUSEACTIONS	
	//*********************************************************************************************************************		
	
	var mouseActionKeys = {};
	var mouseTarget;
	
	var mousePositionX = 0;
	var mousePositionY = 0;
	var mouseDistanceX = 0;
	var mouseDistanceY = 0;
	
		
	function logActionMouseDown(event){		
		
		var timestamp = Date.now();	
				
		if (!(event.which in mouseActionKeys)) {	
		
			mouseActionKeys[event.which] = timestamp;	
			
			//log(aemt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY)
			log("ACTION", "MOUSEDOWN", event.which, event.target.id, "", "", "", "", mouseDistanceX, mouseDistanceY);			
			
		}
				
		mousePositionX = event.clientX;
		mousePositionY = event.clientY;
		mouseDistanceX = 0;
		mouseDistanceY = 0;
	}
	
	function logActionMouseUp(event){			
		
		var timestamp = Date.now();		
						
		if (event.which in mouseActionKeys) {
		
			var actionTime = timestamp - mouseActionKeys[event.which];	
			delete mouseActionKeys[event.which];
			
			//log(aemt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY)
			log("ACTION", "MOUSEUP", event.which, event.target.id, "", "", "", actionTime, mouseDistanceX, mouseDistanceY);		
			
		} else {
		
			//log(aemt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY)
			log("ERROR", "MouseKeyNotFound", event.which, event.target.id, "", "", "", "", "", "");		
			
		}		
				
		mousePositionX = event.clientX;
		mousePositionY = event.clientY;
		mouseDistanceX = 0;
		mouseDistanceY = 0;
	}
	
	function logActionMouseClick(event){	
		//log(aemt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY)
		log("ACTION", "MOUSECLICK", event.which, event.target.id, "", "", "", "", "", "");		
	}
	
	function logActionMouseDoubleClick(event){	
		//log(aemt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY)
		log("ACTION", "MOUSEDOUBLECLICK", event.which, event.target.id, "", "", "", "", "", "");	
	}
	
	
	function logActionMouseMove(event){	
		
		if(mousePositionX == 0 || mousePositionY == 0){
			return;
		}		
		
		mouseDistanceX = mouseDistanceX + Math.abs(mousePositionX - event.clientX);
		mouseDistanceY = mouseDistanceY + Math.abs(mousePositionY - event.clientY);
		
		mousePositionX = event.clientX;
		mousePositionY = event.clientY;				
	}
	
	
	var actionMouseOverTime = 0;
	function logActionMouseOver(id){	
		
		var timestamp = Date.now();		
		
		if(actionMouseOverTime == 0) {
			actionMouseOverTime = timestamp;
			//log(aemt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY)
			log("ACTION", "MOUSEOVER", "enter", id, "", "", "", "", "", "");			
					
		} else {
			var actionTime = timestamp - actionMouseOverTime;	
			actionMouseOverTime = 0;
			
			//log(aemt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY)
			log("ACTION", "MOUSEOVER", "leave", id, "", "", "", actionTime, "", "");
		}			
	}
		
	function logActionMouseWheel(event){	
		
		var delta = Math.max(-1, Math.min(1, (event.wheelDelta || -event.detail)));		 
		
		//log(aemt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY)
		log("ACTION", "MOUSEWHEEL", delta, "", "", "", "", "", "", "");	
	}
		


	//*********************************************************************************************************************		
	//KEYACTIONS
	//*********************************************************************************************************************		
	
	var keyActionKeys = {};
		
	function logActionKeyDown(event){	
		var timestamp = Date.now();		
		
		
		if (event && !(event.which in keyActionKeys)) {	
		
			keyActionKeys[event.which] = timestamp;	
			
			//log(aemt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY)
			log("ACTION", "KEYDOWN", event.which, event.target.id, "", "", "", "", "", "");
		}
	}
	
	function logActionKeyUp(event){	
		var timestamp = Date.now();	
		
		if (event && event.which in keyActionKeys) {
		
			var actionTime = timestamp - keyActionKeys[event.which];	
			delete keyActionKeys[event.which];
			
			//log(aemt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY)
			log("ACTION", "KEYUP", event.which, event.target.id, "", "", "", actionTime, "", "");			
			
		} else {
			//log(aemt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY)
			log("ERROR", "KeyNotFound", event.which, "", "", "", "", "", "", "");			
			
		}
	}
		

	

		
		
	//*********************************************************************************************************************	
	//EVENTS
	//*********************************************************************************************************************	
	
	var eventKeys = {};
	
	function logEvent(type, key){
		//log(aemt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY)
		log("EVENT", type, key, "", "", "", "", "", "", "");	
	}
	
	function logEventTime(type, key){
		var timestamp = Date.now();		
		
		var logString = type + key + targetID;
		if (logString in eventKeys) {
			
			var eventTime = timestamp - eventKeys[logString];	
			delete eventKeys[logString];
			
			//log(aemt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY)
			log("EVENT", type, key, "stop", "", "", "", eventTime, "", "");	
			
		} else {
		
			eventKeys[logString] = timestamp;	

			//log(aemt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY)
			log("EVENT", type, key, "start", "", "", "", "", "", "");	 
			
		}
	}
	
	
	//*********************************************************************************************************************	
	//MANIPULATIONS
	//*********************************************************************************************************************		
	
	function logManipulation(type, key, targetID){
		//log(aemt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY)
		log("MANIPULATION", type, key, targetID, "", "", "", "", "", "");			
	}
	
	
	//*********************************************************************************************************************	
	//TASKS
	//*********************************************************************************************************************		
	
	function logTaskStart(key){
		//log(aemtt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY)
		log("TASK", "Start", key, "", "", "", "", "", "", "");			
		logServerFlush();
	}
	
	function logTaskState (key, missingMarks, falseMarks ){
		//log(aemtt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY)
		log("TASK", "State", key, "", missingMarks, falseMarks, "", "", "", "");			
		logServerFlush();
	}
	
	function logTaskEnd(key, missingMarks, falseMarks ){
		//log(aemtt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY)
		log("TASK", "End", key, "", missingMarks, falseMarks, "", "", "", "");			
		logServerFlush();
	}
	
	
	//*********************************************************************************************************************	
	//TASKS
	//*********************************************************************************************************************		
	function logConfig(clickConnector, clickTransparency, taskOrder ){
		//log(aemtt, type, key, targetID, targetX, targetY, targetZ, duration, mDistanceX, mDistanceY)
		log("CONFIG", clickConnector, clickTransparency, taskOrder, "", "", "", "", "", "");	
	}


	*/
	
	return {
		initialize	: initialize,
		activate	: activate
	};    

})();