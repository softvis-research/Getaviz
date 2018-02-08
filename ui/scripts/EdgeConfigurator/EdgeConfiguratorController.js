var edgeConfiguratorController = (function() {
	
	function initialize(){
	}
	
	function activate(rootDiv){
		//prepareSlider(rootDiv);
		prepareMinimumWeight(rootDiv);
		prepareRelationsCheckbox(rootDiv);
		prepareBundledEdgesCheckbox(rootDiv);
    }
	
	function prepareMinimumWeight(rootDiv) {
		var weightComboBox = "#weightCombobox";
		var divElement = document.createElement("DIV");
		divElement.id = "weightCombobox";
		rootDiv.appendChild(divElement);
		var source = [
			"Show all dependencies",
		    "Only show critical dependencies",
			"Only show very critical dependencies"
		];
		$(weightComboBox).jqxComboBox({ source: source, width: "99%"});
		$(weightComboBox).jqxComboBox({ selectedIndex: 2 });
		$(weightComboBox).on('change', function (event) {
			var args = event.args;
		    if (args) {                       
				var index = args.index;
				var weight = 0;
				switch (index) {
					case 0: weight = 0; break;
					case 1: weight = 0.25; break;
					case 2: weight = 0.5; break;
				}
				var applicationEvent = {			 
					sender: edgeConfiguratorController,
					entities: [weight]
				};
				events.config.weight.publish(applicationEvent);
			}
		});
	}
	
	function prepareBundledEdgesCheckbox(rootDiv) {
		var bundledEdgesCheckboxID = "#bundleCheckbox";
		var divElement = document.createElement("DIV");
		var textNode = document.createTextNode("Bundle edges");
		divElement.id = "bundleCheckbox";
		divElement.appendChild(textNode);
		rootDiv.appendChild(divElement);
		$(bundledEdgesCheckboxID).jqxCheckBox({checked: false});
		$(bundledEdgesCheckboxID).on('checked', function () {
			var applicationEvent = {			 
				sender: edgeConfiguratorController,
				entities: [true]
			};
			events.config.bundledEdges.publish(applicationEvent);
		});
		$(bundledEdgesCheckboxID).on('unchecked', function () {
			var applicationEvent = {			 
				sender: edgeConfiguratorController,
				entities: [false]
			};
			events.config.bundledEdges.publish(applicationEvent);
		});
	}
	
	function prepareRelationsCheckbox(rootDiv) {
		var checkboxID = "#checkbox";
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
		var sliderID = "#edgeSlider";
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
