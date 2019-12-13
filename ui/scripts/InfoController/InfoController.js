var infoController = (function() {
    let controllerConfig = {
        system: "",
        link: "",
        noc: false,
        loc: 0
    };
    
    function initialize(setupConfig){   
        application.transferConfigParams(setupConfig, controllerConfig);
    }

	function activate(){
        var id = "jqxInfoButton";
        var jqxInfoButtonType = "button";
        var jqxInfoButton = document.createElement("BUTTON");
		jqxInfoButton.type = jqxInfoButtonType;
        jqxInfoButton.id = id;
        jqxInfoButton.style = "float:left;";
        var text = document.createTextNode("Info");
        jqxInfoButton.appendChild(text);
        $("ul.jqx-menu-ul")[0].appendChild(jqxInfoButton);
        
        $("#jqxInfoButton").jqxButton({ 
            theme: "metro",
            width: 80, 
            height: 25,
            textImageRelation: "imageBeforeText", 
            textPosition: "left", 
            imgSrc: "scripts/InfoController/images/info.png"
        });

        $("#jqxInfoButton").on('click', function (){
            
            var cssLink = document.createElement("link");
            cssLink.type = "text/css";
            cssLink.rel = "stylesheet";
            cssLink.href = "scripts/InfoController/infoPopup.css";
            document.getElementsByTagName("head")[0].appendChild(cssLink);
             
            if(controllerConfig.system !== "") {
                var system = "<div>System</div>";
                var systemContent = "<div class = 'grid-right infocontroller';>" + controllerConfig.system + "<div>";
            }

            if(controllerConfig.link !== "") {
                var webPage = "<div>Web page</div>";
                var webPageContent = "<div class = 'grid-right infocontroller';>" + "<a href=" + controllerConfig.link + " target='_blank'>" + controllerConfig.link + "</a><div>";
            }

            if(controllerConfig.noc) {
                var classes = "<div>Classes</div>";
                var classesContent = "<div class = 'grid-right infocontroller';>" + model.getEntitiesByType('Class').length + "<div>";
            }

            if(controllerConfig.loc > 0) {
                var loc = "<div>LOC</div>";
                var locContent = "<div class = 'grid-right infocontroller';>" + controllerConfig.loc.toString().length + "<div>";
            }
           
            //var infoTableHTML = "<table ><tr><td class= 'td-grid-left infocontroller'>" + system + "</td><td>" + systemContent + "</td></tr><tr><td class= 'td-grid-left infocontroller'>" + webPage +"</td><td>" + webPageContent + "</td></tr><tr><td class= 'td-grid-left infocontroller'>" + classes + "</td><td>" + classesContent + "</td></tr><tr><td class= 'td-grid-left infocontroller'>" + loc + "</td><td>" + locContent + "</td></tr></table>";
            
            var infoTableHTML = "<table><tr><td class= 'td-grid-left infocontroller'>System</td><td>" + systemContent + "</td></tr><tr><td class= 'td-grid-left infocontroller'>Web page</td><td>" + webPageContent + "</td></tr><tr><td class= 'td-grid-left infocontroller'> Classes</td><td>" + classesContent + "</td></tr><tr><td class= 'td-grid-left infocontroller'>LOC</td><td>" + locContent + "</td></tr></table>";

       
            $("#DisplayWindow").remove();
            var popup = application.createPopup("Info", infoTableHTML, "DisplayWindow");
            document.body.appendChild(popup);
            $("#DisplayWindow").css("display", "block").jqxWindow({
                    theme: "metro",
                    width: 470,
                    height: 270,
                    isModal: true,
                    autoOpen: true,
                    resizable: false
            });
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
