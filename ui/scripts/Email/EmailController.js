var emailController = (function() {
	
	var logObjectMap = new Map();

	//config parameters		
	var controllerConfig = {		
		createHeadSection: true
	}
	
	
	function initialize(setupConfig){	

		application.transferConfigParams(setupConfig, controllerConfig);

    }
	
	function activate(rootDiv){

		createMailPopup(rootDiv);
		
		$("#emailPoupWindowDiv").jqxWindow({ theme: "metro", width: 400, height: 500, isModal: true, autoOpen: false, resizable: false, cancelButton: $("#cancelEmail"), initContent: function() {
				// add UI elements to EmailPopupWindow
				$("#emailTextArea").jqxTextArea({ placeHolder: 'What do you want us to say? :)', width: 300, height: 400, minLength: 1});
				$("#sendEmail").jqxButton({ theme: "metro", width: "50px" });
				$("#cancelEmail").jqxButton({ theme: "metro", width: "50px" });
            }
        });
		
		$("#sendEmail").click(function(event) {
			var message = $('#emailTextArea').val();			
			sendMail(message);
			$("#emailPoupWindowDiv").jqxWindow("close");
		});

		

		events.log.info.subscribe(addLogObject);
		events.log.warning.subscribe(addLogObject);
		events.log.error.subscribe(addLogObject);
		events.log.action.subscribe(addLogObject);
		events.log.event.subscribe(addLogObject);
		events.log.manipulation.subscribe(addLogObject);		
	}
	
	function reset(){		
		
	}
	
	function openMailPopUp(){
		$("#emailPoupWindowDiv").jqxWindow("open");
	}


	function addLogObject(logObject){
		
		if(!logObjectMap.has(logObject.eventType)){
			logObjectMap.set(logObject.eventType, new Array());
		}
		
		var logObjectTypeArray = logObjectMap.get(logObject.eventType);
		

		if(logObjectTypeArray.length >= 1000){
			logObjectTypeArray.shift();
		}
		logObjectTypeArray.push(logObject);
	}

	function createHeadSection(rootDiv){

		var cssLink = document.createElement("link");
		cssLink.type = "text/css";
		cssLink.rel = "stylesheet";
		cssLink.href = "scripts/Email/email.css";
		document.getElementsByTagName("head")[0].appendChild(cssLink);

		//container div
		headControllerDiv = document.createElement("DIV");
		rootDiv.appendChild(headControllerDiv);
		
		//headFieldText and solvedButton
		var headDiv = document.createElement("DIV");
		headControllerDiv.appendChild(headDiv);
		headDiv.id = "headField";
		
		
		var headFieldTextDiv = document.createElement("DIV");
		headDiv.appendChild(headFieldTextDiv);
		headFieldTextDiv.id = "headFieldText";
		headFieldTextDiv.innerHTML = 
			"Welcome to the evaluation prototype of generative software visualization!" + "<br/>" +
			"" + "<br/>" + 
			"For further information click <a href='https://www.wifa.uni-leipzig.de/en/information-systems-institute/se/research/softwarevisualization-in-3d-and-vr.html'> here. </a>" + "<br/>" +  
			"" + "<br/>" + 
			"If you want to give us feedback, report a bug or suggest a new feature, please use the 'Send us a mail!' button on the right side ->";
		
		
		var sendEmailButton = document.createElement("INPUT");
		headDiv.appendChild(sendEmailButton);
		sendEmailButton.id = "sendMailButton";
		sendEmailButton.value = "Send us a mail!";
		sendEmailButton.type = "button";				

	}


	function createMailPopup(rootDiv){

		var emailPoupWindowDiv = document.createElement("DIV");
        rootDiv.appendChild(emailPoupWindowDiv);
        emailPoupWindowDiv.id = "emailPoupWindowDiv";

            var emailPoupTitleDiv = document.createElement("DIV");            
            emailPoupWindowDiv.appendChild(emailPoupTitleDiv);
            emailPoupTitleDiv.innerHTML = "Feedback";

            var emailPoupContentDiv = document.createElement("DIV");
            emailPoupWindowDiv.appendChild(emailPoupContentDiv);
                
				//Text Area
				var emailPoupTextArea= document.createElement("TEXTAREA");
                emailPoupContentDiv.appendChild(emailPoupTextArea);

				emailPoupTextArea.id = "emailTextArea";
					                            
				//Buttons
                var emailPoupApplyInput = document.createElement("INPUT");
                emailPoupContentDiv.appendChild(emailPoupApplyInput);
                emailPoupApplyInput.type = "button";
                emailPoupApplyInput.id = "sendEmail";
                emailPoupApplyInput.value = "Send";                

                var emailPoupCancelInput = document.createElement("INPUT");
                emailPoupContentDiv.appendChild(emailPoupCancelInput);
                emailPoupCancelInput.type = "button";
                emailPoupCancelInput.id = "cancelEmail";
                emailPoupCancelInput.value = "Cancel";

	}

	
	function sendMail(message){

		var logDump = "";
		logObjectMap.forEach(function(logObjectTypeArray, key, map) {

			logDump = logDump +  key.type; + " \r\n";
			logDump = logDump +  "****************" + " \r\n" + " \r\n";

			logObjectTypeArray.forEach(function(logObject, index, array) {

				logDump = logDump + logObject.timeStamp + " - ";
				if(logObject.actionObject){
					logDump = logDump + logObject.actionObject.type + " \r\n";
				} else {  

					if(logObject.applicationEvent){
						logDump = logDump + logObject.applicationEvent.eventType.name;
						
						if(logObject.applicationEvent.entities[0]){
							logDump = logDump + " : " + logObject.applicationEvent.entities[0].qualifiedName;
							logDump = logDump + " ; " + logObject.applicationEvent.entities[0].id;
						} else {
							logDump = logDump + " : " + "error reading entitiy - " + logObject.text;
						}
						
						logDump = logDump + " \r\n";
					} else {

						logDump = logDump + logObject.text + " \r\n";

					}
				}
				
			});
		});

		var logFile = Date.now() + ".txt";

		$.ajax({
    		url: "scripts/Email/mail.php", 
    		method: "POST",
    		data: { name: "SVis", 
					email: "pascal.kovacs@gmx.de", 
					message: message, 
					logDump: logDump,
					logFile: logFile
			}
    	}).success(function(data, status, headers, config) {
   
			// this callback will be called asynchronously
			// when the response is available
			if(status == 200) {

				var return_data = data;

				if(return_data != 0){

				}
			}
		}).error(function(data, status, headers, config) {
   
			// called asynchronously if an error occurs
			// or server returns response with an error status.
   			
			console.log(status);
   
   	 	});
	}
	

    return {
        initialize: initialize,
		activate: activate,

		openMailPopUp: openMailPopUp
    };    
})();