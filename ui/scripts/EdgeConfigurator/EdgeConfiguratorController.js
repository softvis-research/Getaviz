var edgeConfiguratorController = (function() {
	
	function initialize(){
	}
	
	function activate(rootDiv){
        rootDiv.setAttribute("style","height:100%");

        prepareVisibilityLevelMenu(rootDiv);
		prepareBundledEdgesCheckbox(rootDiv);
    }

    function prepareVisibilityLevelMenu(rootDiv){
        var a = document.createElement('a');
        var text = document.createTextNode("Visibility Level of Circular Dependencies");
        a.appendChild(text);
        a.href ="javascript: window.open(\"./glossary.html#level\",'glossary');";
        rootDiv.appendChild(a);
        prepareRadioButton(rootDiv, "showall", "Show all dependencies", 0);
        prepareRadioButton(rootDiv, "showcritical", "Only show critical dependencies", 0.25);
        prepareRadioButton(rootDiv, "showverycritical", "Only show very critical dependencies", 0.5);
        $('#showverycritical').jqxRadioButton('check');
    }

    function prepareRadioButton(rootDiv, id, text, value){
        var jqxId = "#" + id;
        var divElement = document.createElement("DIV");
        var textNode = document.createTextNode(text);
        divElement.appendChild(textNode);
        divElement.id = id;
        rootDiv.appendChild(divElement);
        $(jqxId).jqxRadioButton({ theme: "metro"});
        $(jqxId).bind('checked', function (event) {
            var applicationEvent = {
                sender: edgeConfiguratorController,
                entities: [value]
            };
            events.config.weight.publish(applicationEvent);
        });
    }
	
	function prepareBundledEdgesCheckbox(rootDiv) {
		var bundledEdgesCheckboxID = "#bundleCheckbox";
		var divElement = document.createElement("DIV");
		var textNode = document.createTextNode("Bundle dependencies");
		divElement.id = "bundleCheckbox";
		divElement.appendChild(textNode);
		rootDiv.appendChild(divElement);
		$(bundledEdgesCheckboxID).jqxCheckBox({theme: "metro", checked: false});
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
	
	function reset(){
	}
    
    return {
        initialize: initialize,
		activate: activate,
		reset: reset
    };
})();
