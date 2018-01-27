var edgeConfiguratorController = (function() {

	var sliderID = "#edgeSlider";
	var checkboxID = "#checkbox";
	
	function initialize(){
	}
	
	function activate(rootDiv){
		prepareSlider(rootDiv);
		
		var divElement = document.createElement("DIV");
		var textNode = document.createTextNode("Show inner relations");
		divElement.id = "checkbox";
		divElement.appendChild(textNode);
		rootDiv.appendChild(divElement);
		$(checkboxID).jqxCheckBox({checked: true});
		$(checkboxID).on('checked', function () {
			var applicationEvent = {			 
				sender: edgeConfiguratorController,
				entities: [true]
			};
			events.config.innerClasses.publish(applicationEvent);
		});
		$(checkboxID).on('unchecked', function () {
			var applicationEvent = {			 
				sender: edgeConfiguratorController,
				entities: [false]
			};
			events.config.innerClasses.publish(applicationEvent);
		});
    }
	
	function prepareSlider(rootDiv) {
		var myDiv = document.createElement("H3");
		var newContent = document.createTextNode("Minimal Edge Weight");
		myDiv.appendChild(newContent);
		rootDiv.appendChild(myDiv);
		
		var sliderDiv = document.createElement("DIV");	
		sliderDiv.id = "edgeSlider";
		rootDiv.appendChild(sliderDiv);
		$(sliderID).on('change', function (event) {
			var value = event.args.value;
			var applicationEvent = {			 
				sender: edgeConfiguratorController,
				entities: [value]
			};
			events.config.weight.publish(applicationEvent);
		});
		$(sliderID).jqxSlider({
            showTickLabels: true, tooltip: false, mode: "fixed", ticksPosition: "bottom", height: 60, min: 0, max: 1, ticksFrequency: 0.25, step: 0.125,value: 0.5,
			tickLabelFormatFunction: function (value) {
		      if (value == 0) return value;
              if (value == 1) return value;
              return "";
			}
        });
	}
	
	function reset(){
	}
    
    return {
        initialize: initialize,
		activate: activate,
		reset: reset
    };
})();
