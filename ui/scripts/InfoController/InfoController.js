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
        createInfoButton();
        createInfoPopup();
    }
    
    function createInfoButton(){
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
 	}
 	
    function createInfoPopup(){
        $("#jqxInfoButton").on('click', function (){
            createCSSLink();
            
            var infoTableHTML = "<table id='infoTable'></table>";
            $("#DisplayWindow").remove();
            var popup = application.createPopup("Info", infoTableHTML, "DisplayWindow");
            document.body.appendChild(popup);
            $("#DisplayWindow").css("display", "block").jqxWindow({
                theme: "metro",
                width: 480,
                height: 210,
                isModal: true,
                autoOpen: true,
                resizable: false
            });
            
            if(controllerConfig.system !== "") {
                createSystemRow();
            } 

            if(controllerConfig.link !== "") {
                createWebPageRow();
            } 

            if(controllerConfig.noc) {
                createClassesRow();
            } 

            if(controllerConfig.loc > 0) {
                createLocRow();
            }
        });
 	}

    function createCSSLink(){
        var cssLink = document.createElement("link");
        cssLink.type = "text/css";
        cssLink.rel = "stylesheet";
        cssLink.href = "scripts/InfoController/style.css";
        document.getElementsByTagName("head")[0].appendChild(cssLink);        
    }
    
    function createSystemRow(){
        var tr_System = document.createElement("tr");
        var td_System_Name = document.createElement("td");
        td_System_Name.setAttribute("class", "td-grid-left infoController");
        var td_System_Value = document.createElement("td");
                
        var system = document.createElement("div");
        var text_System = document.createTextNode("System");
        system.appendChild(text_System);
                
        var systemContentDiv = document.createElement("div");
        systemContentDiv.setAttribute("class", "grid-right infoController");
        var systemContent = controllerConfig.system;
        systemContentDiv.innerHTML = systemContent;
                
        td_System_Name.appendChild(system);
        td_System_Value.appendChild(systemContentDiv);
        tr_System.appendChild(td_System_Name);
        tr_System.appendChild(td_System_Value);
                
        document.getElementById("infoTable").appendChild(tr_System);
 	}
 	
    function createWebPageRow(){
        var tr_WebPage = document.createElement("tr");
        var td_WebPage_Name = document.createElement("td");
        td_WebPage_Name.setAttribute("class", "td-grid-left infoController");
        var td_WebPage_Value = document.createElement("td");
                
        var webPage = document.createElement("div");
        var text_WebPage = document.createTextNode("WebPage");
        webPage.appendChild(text_WebPage);
                
        var webPageContentDiv = document.createElement("div");
        webPageContentDiv.setAttribute("class", "grid-right infoController");
        var webPageContent = controllerConfig.link;
        webPageContentDiv.innerHTML = webPageContent;
                
        td_WebPage_Name.appendChild(webPage);
        td_WebPage_Value.appendChild(webPageContentDiv);
        tr_WebPage.appendChild(td_WebPage_Name);
        tr_WebPage.appendChild(td_WebPage_Value);
                
        document.getElementById("infoTable").appendChild(tr_WebPage); 
 	}
 	
    function createClassesRow(){
        var tr_Classes = document.createElement("tr");
        var td_Classes_Name = document.createElement("td");
        td_Classes_Name.setAttribute("class", "td-grid-left infoController");
        var td_Classes_Value = document.createElement("td");
                
        var classes = document.createElement("div");
        var text_Classes = document.createTextNode("Classes");
        classes.appendChild(text_Classes);
                
        var classesContentDiv = document.createElement("div");
        classesContentDiv.setAttribute("class", "grid-right infoController");
        var classesContent = model.getEntitiesByType('Class').length;
        classesContentDiv.innerHTML = classesContent;
                
        td_Classes_Name.appendChild(classes);
        td_Classes_Value.appendChild(classesContentDiv);
        tr_Classes.appendChild(td_Classes_Name);
        tr_Classes.appendChild(td_Classes_Value);
                
        document.getElementById("infoTable").appendChild(tr_Classes); 
 	}
 	
    function createLocRow(){
        var tr_LOC = document.createElement("tr");
        var td_LOC_Name = document.createElement("td");
        td_LOC_Name.setAttribute("class", "td-grid-left infoController");
        var td_LOC_Value = document.createElement("td");
                
        var loc = document.createElement("div");
        var text_LOC = document.createTextNode("LOC");
        loc.appendChild(text_LOC);
                
        var locContentDiv = document.createElement("div");
        locContentDiv.setAttribute("class", "grid-right infoController");
        var locContent = controllerConfig.loc
        locContentDiv.innerHTML = locContent;
                
        td_LOC_Name.appendChild(loc);
        td_LOC_Value.appendChild(locContentDiv);
        tr_LOC.appendChild(td_LOC_Name);
        tr_LOC.appendChild(td_LOC_Value);
                
        document.getElementById("infoTable").appendChild(tr_LOC); 
 	}
 	
 	function reset(){
 	}
	
    return {
        initialize: initialize,
		activate: activate,
		reset: reset
	};    
})();
