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
        jqxInfoButton.style = "float:right;";
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
             
            //var infoTableHTML = "<table id='infoTable'><tr id ='trContent0'><td id ='tdContent0-left' class= 'td-grid-left infoController'></td><td id ='tdContent0-right'></td></tr><tr id ='trContent1'><td id ='tdContent1-left' class= 'td-grid-left infoController'></td><td id ='tdContent1-right'></td></tr><tr id ='trContent2'><td id ='tdContent2-left' class= 'td-grid-left infoController'></td><td id ='tdContent2-right'></td></tr><tr id ='trContent3'><td id ='tdContent3-left' class= 'td-grid-left infoController'></td><td id ='tdContent3-right'></td></tr></table>";
            
            var infoTableHTML = "<table id='infoTable'></table>";
            
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
            
            /*if(controllerConfig.system !== "") {
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
            }*/
            
            if(controllerConfig.system !== "") {
                var tr_System = document.createElement("tr");
                var td_System_Left = document.createElement("td");
                td_System_Left.setAttribute("class", "td-grid-left infoController");
                var td_System_Right = document.createElement("td");
                
                var system = document.createElement("div");
                var text_System = document.createTextNode("System");
                system.appendChild(text_System);
                
                var systemContentDiv = document.createElement("div");
                systemContentDiv.setAttribute("class", "grid-right infoController");
                var systemContent = controllerConfig.system;
                systemContentDiv.innerHTML = systemContent;
                
                td_System_Left.appendChild(system);
                td_System_Right.appendChild(systemContentDiv);
                tr_System.appendChild(td_System_Left);
                tr_System.appendChild(td_System_Right);
                
                document.getElementById("infoTable").appendChild(tr_System);
            } 

            if(controllerConfig.link !== "") {
                var tr_WebPage = document.createElement("tr");
                var td_WebPage_Left = document.createElement("td");
                td_WebPage_Left.setAttribute("class", "td-grid-left infoController");
                var td_WebPage_Right = document.createElement("td");
                
                var webPage = document.createElement("div");
                var text_WebPage = document.createTextNode("WebPage");
                webPage.appendChild(text_WebPage);
                
                var webPageContentDiv = document.createElement("div");
                webPageContentDiv.setAttribute("class", "grid-right infoController");
                var webPageContent = controllerConfig.link;
                webPageContentDiv.innerHTML = webPageContent;
                
                td_WebPage_Left.appendChild(webPage);
                td_WebPage_Right.appendChild(webPageContentDiv);
                tr_WebPage.appendChild(td_WebPage_Left);
                tr_WebPage.appendChild(td_WebPage_Right);
                
                document.getElementById("infoTable").appendChild(tr_WebPage); 
            } 

            if(controllerConfig.noc) {
                var tr_Classes = document.createElement("tr");
                var td_Classes_Left = document.createElement("td");
                td_Classes_Left.setAttribute("class", "td-grid-left infoController");
                var td_Classes_Right = document.createElement("td");
                
                var classes = document.createElement("div");
                var text_Classes = document.createTextNode("Classes");
                classes.appendChild(text_Classes);
                
                var classesContentDiv = document.createElement("div");
                classesContentDiv.setAttribute("class", "grid-right infoController");
                var classesContent = model.getEntitiesByType('Class').length;
                classesContentDiv.innerHTML = classesContent;
                
                td_Classes_Left.appendChild(classes);
                td_Classes_Right.appendChild(classesContentDiv);
                tr_Classes.appendChild(td_Classes_Left);
                tr_Classes.appendChild(td_Classes_Right);
                
                document.getElementById("infoTable").appendChild(tr_Classes); 
            } 

            if(controllerConfig.loc > 0) {
                var tr_LOC = document.createElement("tr");
                var td_LOC_Left = document.createElement("td");
                td_LOC_Left.setAttribute("class", "td-grid-left infoController");
                var td_LOC_Right = document.createElement("td");
                
                var loc = document.createElement("div");
                var text_LOC = document.createTextNode("LOC");
                loc.appendChild(text_LOC);
                
                var locContentDiv = document.createElement("div");
                locContentDiv.setAttribute("class", "grid-right infoController");
                var locContent = controllerConfig.loc.toString().length
                locContentDiv.innerHTML = locContent;
                
                td_LOC_Left.appendChild(loc);
                td_LOC_Right.appendChild(locContentDiv);
                tr_LOC.appendChild(td_LOC_Left);
                tr_LOC.appendChild(td_LOC_Right);
                
                document.getElementById("infoTable").appendChild(tr_LOC); 
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
