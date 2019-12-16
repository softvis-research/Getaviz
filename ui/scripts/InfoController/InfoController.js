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
             
            var infoTableHTML = "<table id='infoTable'><tr id ='trContent0'><td id ='tdContent0-left' class= 'td-grid-left infoController'></td><td id ='tdContent0-right'></td></tr><tr id ='trContent1'><td id ='tdContent1-left' class= 'td-grid-left infoController'></td><td id ='tdContent1-right'></td></tr><tr id ='trContent2'><td id ='tdContent2-left' class= 'td-grid-left infoController'></td><td id ='tdContent2-right'></td></tr><tr id ='trContent3'><td id ='tdContent3-left' class= 'td-grid-left infoController'></td><td id ='tdContent3-right'></td></tr></table>";

            $("#DisplayWindow").remove();
            var popup = application.createPopup("Info", infoTableHTML, "DisplayWindow");
            document.body.appendChild(popup);
            $("#DisplayWindow").css("display", "block").jqxWindow({
                    theme: "metro",
                    width: 435,
                    height: 210,
                    isModal: true,
                    autoOpen: true,
                    resizable: false
            });
            
            if(controllerConfig.system !== "") {
                var system = "<div>System</div>";
                var systemContent = "<div class = 'grid-right infoController';>" + controllerConfig.system + "<div>";
                document.getElementById('tdContent0-left').innerHTML= system;
                document.getElementById('tdContent0-right').innerHTML= systemContent;
            } else{
                document.getElementById("trContent0").remove();
            }

            if(controllerConfig.link !== "") {
                var webPage = "<div>Web page</div>";
                var webPageContent = "<div class = 'grid-right infoController';>" + "<a href=" + controllerConfig.link + " target='_blank'>" + controllerConfig.link + "</a><div>";
                document.getElementById('tdContent1-left').innerHTML= webPage;
                document.getElementById('tdContent1-right').innerHTML= webPageContent;
            } else{
                document.getElementById("trContent1").remove();
            }

            if(controllerConfig.noc) {
                var classes = "<div>Classes</div>";
                var classesContent = "<div class = 'grid-right infoController';>" + model.getEntitiesByType('Class').length + "<div>";
                document.getElementById('tdContent2-left').innerHTML= classes;
                document.getElementById('tdContent2-right').innerHTML= classesContent;
            } else{
                document.getElementById("trContent2").remove();
            }

            if(controllerConfig.loc > 0) {
                var loc = "<div>LOC</div>";
                var locContent = "<div class = 'grid-right infoController';>" + controllerConfig.loc.toString().length + "<div>";
                document.getElementById('tdContent3-left').innerHTML= loc;
                document.getElementById('tdContent3-right').innerHTML= locContent;
            } else{
                document.getElementById("trContent3").remove();
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
