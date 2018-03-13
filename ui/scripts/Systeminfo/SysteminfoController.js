var systeminfoController = (function() {

    var controllerConfig = {
        system: "",
        link: "",
        visualization: "",
    };

	function initialize(setupConfig){
        application.transferConfigParams(setupConfig, controllerConfig);
    }
	
	function activate(rootDiv){
	    var container= document.createElement("DIV");
	    container.setAttribute("class", "grid-container");
	    var g1 = document.createElement("div");
	    var g2 = document.createElement("div");
	    var g3 = document.createElement("div");
	    var g4 = document.createElement("div");
	    var g5 = document.createElement("div");
	    var g6 = document.createElement("div");

	    g1.setAttribute("class", "grid-item-left")
	    g2.setAttribute("class", "grid-item-right")
	    g3.setAttribute("class", "grid-item-left")
	    g4.setAttribute("class", "grid-item-right")
	    g5.setAttribute("class", "grid-item-left")
	    g6.setAttribute("class", "grid-item-right")

		var nameElement1 = document.createTextNode("System");
		var nameElement2= document.createTextNode(controllerConfig.system);

		var linkElement1 = document.createTextNode("Web page");
        var linkElement2 = document.createTextNode(controllerConfig.link);
        var a = document.createElement("a");
        a.href = controllerConfig.link;
        a.target = "_blank";
        a.appendChild(linkElement2)

        var visualizationElement1 = document.createTextNode("Visualization");
        var visualizationElement2 = document.createTextNode(controllerConfig.visualization);


        g1.appendChild(nameElement1)
        g2.appendChild(nameElement2)
        g3.appendChild(linkElement1)
        g4.appendChild(a)
        g5.appendChild(visualizationElement1)
        g6.appendChild(visualizationElement2)

        container.appendChild(g1)
        container.appendChild(g2)
        container.appendChild(g3)
        container.appendChild(g4)
      //  container.appendChild(g5)
       // container.appendChild(g6)

		rootDiv.appendChild(container);
	}
	
	function reset(){
	}

    return {
        initialize: initialize,
		activate: activate,
		reset: reset
    };
})();
