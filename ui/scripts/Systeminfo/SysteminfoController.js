var systeminfoController = (function() {

    let controllerConfig = {
        system: "",
        link: "",
        noc: false,
        loc: 0
    };

	function initialize(setupConfig){
        application.transferConfigParams(setupConfig, controllerConfig);
    }
	
	function activate(rootDiv){
	    let container= document.createElement("DIV");
	    container.setAttribute("class", "grid-container");

        if(controllerConfig.system !== "") {
            let leftCell = document.createElement("div");
            let rightCell = document.createElement("div");
            leftCell.setAttribute("class", "grid-item-left");
            rightCell.setAttribute("class", "grid-item-right");
            let nameElement1 = document.createTextNode("System");
            let nameElement2 = document.createTextNode(controllerConfig.system);
            leftCell.appendChild(nameElement1);
            rightCell.appendChild(nameElement2);
            container.appendChild(leftCell);
            container.appendChild(rightCell);

        }

        if(controllerConfig.link !== "") {
            let leftCell = document.createElement("div");
            let rightCell = document.createElement("div");
            leftCell.setAttribute("class", "grid-item-left");
            rightCell.setAttribute("class", "grid-item-right");
            let linkElement1 = document.createTextNode("Web page");
            let link = document.createTextNode(controllerConfig.link);
            let linkElement2 = document.createElement("a");
            linkElement2.href = controllerConfig.link;
            linkElement2.target = "_blank";
            linkElement2.appendChild(link);
            leftCell.appendChild(linkElement1);
            rightCell.appendChild(linkElement2);
            container.appendChild(leftCell);
            container.appendChild(rightCell);
        }

        if(controllerConfig.noc) {
            let leftCell = document.createElement("div");
            let rightCell = document.createElement("div");
            leftCell.setAttribute("class", "grid-item-left");
            rightCell.setAttribute("class", "grid-item-right");
            let nocElement1 = document.createTextNode("Classes");
            let nocElement2 = document.createTextNode(model.getEntitiesByType("Class").length);
            leftCell.appendChild(nocElement1);
            rightCell.appendChild(nocElement2);
            container.appendChild(leftCell);
            container.appendChild(rightCell);
        }

        if(controllerConfig.loc > 0) {
            let leftCell = document.createElement("div");
            let rightCell = document.createElement("div");
            leftCell.setAttribute("class", "grid-item-left");
            rightCell.setAttribute("class", "grid-item-right");
            let locElement1 = document.createTextNode("LOC");
            let locElement2 = document.createTextNode(controllerConfig.loc.toString());
            leftCell.appendChild(locElement1);
            rightCell.appendChild(locElement2);
            container.appendChild(leftCell);
            container.appendChild(rightCell);
        }

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
