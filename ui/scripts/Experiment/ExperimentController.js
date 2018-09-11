var experimentController = (function() {	
		
	var experimentControllerDiv;
	
	var stepOrder;
	var stepOrderIterator = 0;
	
	var steps;	
	var currentStep;
	
	var stepTime = 0;
	var stepTextTime = 0;

	var controllerConfig = {
		showBackButton: false,
		showSureButton: true,
		showPopup: true,
	};


    function initialize(setupConfig){

        application.transferConfigParams(setupConfig, controllerConfig);

        var cssLink = document.createElement("link");
		cssLink.type = "text/css";
		cssLink.rel = "stylesheet";
		cssLink.href = "scripts/Experiment/ec.css";
		document.getElementsByTagName("head")[0].appendChild(cssLink);
				
		//interactionLogger.logConfig(config.clickConnector, config.clickTransparency, config.taskOrder.toString());

		stepOrder = setupConfig.stepOrder;
		steps = setupConfig.steps;
		
		stepTextTime = setupConfig.taskTextButtonTime;
		stepTime = setupConfig.taskTime;
		
		//events
		events.marked.on.subscribe(onEntityMarked);
		events.marked.off.subscribe(onEntityMarked);
		
		//container div
		experimentControllerDiv = document.createElement("DIV");
		
		//taskFieldText and solvedButton
		var experimentHeaderDiv = document.createElement("DIV");
		experimentHeaderDiv.id = "taskField";
		experimentControllerDiv.appendChild(experimentHeaderDiv);
		
		var taskFieldTextDiv = document.createElement("DIV");
		taskFieldTextDiv.id = "taskFieldText";
		taskFieldTextDiv.innerHTML = "Step";
		experimentHeaderDiv.appendChild(taskFieldTextDiv);
		
		var taskSolvedButton = document.createElement("INPUT");
		taskSolvedButton.id = "taskSolvedButton";
		taskSolvedButton.value = "Next";
		taskSolvedButton.type = "button";		
		experimentHeaderDiv.appendChild(taskSolvedButton);

		if(controllerConfig.showBackButton) {
            var backButton = document.createElement('INPUT');
            backButton.id = 'backButton';
            backButton.value = 'Back';
            backButton.type = 'button';
            experimentHeaderDiv.appendChild(backButton);
        }
		
		//taskdialog
		var taskDialogDiv = document.createElement("DIV");
		taskDialogDiv.id = "taskDialog";
		taskDialogDiv.style = "display:none";
		experimentControllerDiv.appendChild(taskDialogDiv);
		
		var taskDialogTitleDiv = document.createElement("DIV");
		taskDialogTitleDiv.innerHTML = "Step";
		taskDialogDiv.appendChild(taskDialogTitleDiv);
		
		var taskDialogTextDiv = document.createElement("DIV");
		taskDialogDiv.appendChild(taskDialogTextDiv);
		
		var taskDialogTextH3 = document.createElement("H3");
		taskDialogTextH3.id = "taskText";
		taskDialogTextH3.innerHTML = "TestText";
		taskDialogTextDiv.appendChild(taskDialogTextH3);
		
		var taskDialogOkButton = document.createElement("INPUT");
		taskDialogOkButton.id = "button_ok";
		taskDialogOkButton.value = "OK";
		taskDialogOkButton.type = "button";		
		taskDialogTextDiv.appendChild(taskDialogOkButton);	
							
	}
	
	
	function activate(parent){
		
		parent.appendChild(experimentControllerDiv);
		
		//taskFieldText and solvedButton
		$('#taskSolvedButton').jqxButton({ theme: 'metro' });
		$('#taskSolvedButton').click(taskSolvedButtonClick);

		if(controllerConfig.showBackButton) {
            $('#backButton').jqxButton({theme: 'metro'});
            $('#backButton').click(backButtonClick);
        }
		
		//taskdialog
		$("#taskDialog").jqxWindow({ height: 1000, width: 700, theme: 'metro', isModal: true, autoOpen: false, resizable: false, showCloseButton: false, okButton: $('#button_ok') });
		$("#button_ok").jqxButton({ theme: "metro", width: "50px" });		
		$("#button_ok").click(function () {		
			if(stepTime != 0){
				startTaskTimer(stepTime);		
			}
			$("#taskDialog").jqxWindow('close');
		});
						
		//initialize first step
		setNextStep();
		setStepTexts(currentStep.text, 100, 100, 1000, 300, stepTextTime);		
		
		setTimeout(taskTimer, 1000);
	}
	
	
	function taskSolvedButtonClick(event) {

        if ($("#taskSolvedButton")[0].value == "Next" && controllerConfig.showSureButton) {
            $("#taskSolvedButton")[0].value = "Sure?"
            setTimeout(resetSolvedButton, 3000);
        } else {

            nextStep();
        }
    }

	function backButtonClick(event) {
		previousStep();
	}

	function resetSolvedButton() {
		if ($('#taskSolvedButton')[0].value !== 'Next') $('#taskSolvedButton')[0].value = 'Next';
	}
	
	function nextStep(){
		
		stopTaskTimer();
		
		setNextStep();
		
		setStepTexts(currentStep.text, 100, 100, 1000, 300, stepTextTime);		
	}
		
	function previousStep() {
		stopTaskTimer();

		setPreviousStep();

		setStepTexts(currentStep.text, 100, 100, 1000, 300, stepTextTime);
	}
		
	function setNextStep(){
		
		stepOrderIterator = stepOrderIterator + 1;
		
		var nextStepByStepOrder = stepOrder[stepOrderIterator-1];
		
		steps.forEach(function(step){
			if(step.number == nextStepByStepOrder){
				currentStep = step;
				return;
			}
		});		
	}
	
	function setPreviousStep() {
		if (stepOrderIterator > 1) {
			stepOrderIterator = stepOrderIterator - 1;

			var nextStepByStepOrder = stepOrder[stepOrderIterator - 1];

			steps.forEach(function(step) {
				if (step.number == nextStepByStepOrder) {
					currentStep = step;
					return;
				}
			});
		}
	}
	
	function setStepTexts(textArray, posx, posy, width, height, time){
		
		var fullText = "";
		
		textArray.forEach(function(text){
			fullText = fullText + text + "<br/>";
		});
		if(controllerConfig.showPopup) {
            showPopup(fullText, posx, posy, width, height, time);
        }
		setText(fullText);				
	}
	
	function showPopup(text, posx, posy, width, height, time){
		//open task dialog
		$("#taskText").html(text);
		
		if(time != 0){
			$("#button_ok").jqxButton({ disabled: true });		
			setTimeout(timeoutButton, 1000);			
			timeout = time;
		}
		
		$("#taskDialog").jqxWindow({ position: { x: posx, y: posy }}); 
		$("#taskDialog").jqxWindow({ height: height, width: width, maxWidth: width});
		$("#taskDialog").jqxWindow('open');				
	}
	
	var timeout = 1;
	
	function timeoutButton(){
		if(timeout == 0){
			$("#button_ok").jqxButton({disabled: false}); 
			$("#button_ok")[0].value = "OK";				
		} else {
			timeout = timeout - 1;
			$("#button_ok")[0].value = timeout;
			setTimeout(timeoutButton, 1000);
		}
	}
	
	
	function setText(text){
		//set task field
		$("#taskFieldText").html(text);	
		$("#taskFieldText").css("text-transform", "none");		
	}
	
	
	//function resetSolvedButton(){
	//	$("#taskSolvedButton")[0].value = "Next";
	//}
	
	
	
	
	
			
	
	//timout after task time
	//**********************
	
	var taskTimerOn = false;
	var timeOutTime = 0;
	
	function taskTimer(){
				
		setTimeout(taskTimer, 1000);
		
		if(!taskTimerOn){			
			return;
		}
		var timeNow = Date.now();
		if(timeNow >  timeOutTime){
			nextStep();
		}
	}
	
		
	function startTaskTimer(timeoutInMin){
		timeOutTime = Date.now() + ( timeoutInMin * 60 * 1000);	
		taskTimerOn = true;
	}
	
	function stopTaskTimer(){
		taskTimerOn = false;		
	}
	
	
	
	
	
	
	
	//log task states
	//***************
	function onEntityMarked(applicationEvent) {		
		if(!currentStep.entities){
			return;
		}
		
		var taskState = getTaskState();		

		var entity = applicationEvent.entities[0];		
		
		events.log.controller.publish({ text: "experimentController", var1: currentStep.number, var2: taskState.missingMarks, var3: taskState.falseMarks, var4: entity.qualifiedName });
	}
	
	function getTaskState(){
		
		var markedEntites = events.marked.getEntities();
		
		var taskEntitiesIds = currentStep.entities;			
		
		var	correctMarks = 0;			
		var falseMarks = 0;
		var missingMarks = 0;			
		
		for(var i = 0; i < taskEntitiesIds.length; i++) {				
			if(markedEntites.has(taskEntitiesIds[i])){
				correctMarks++;
			} else {
				missingMarks++;
			}				
		}
		
		falseMarks = markedEntites.size - correctMarks;
		
		return {
			missingMarks: missingMarks,
			falseMarks: falseMarks
		};
	
	}
	
    
    
    return {
        initialize: initialize,
		activate: activate
    };
}
)();


