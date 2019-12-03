var helpController = (function() {
    let controllerConfig = {
        metaphor: "",
	};
    
    function initialize(setupConfig){   
        
        application.transferConfigParams(setupConfig, controllerConfig);
    }
	
	function activate(){
    
        var id = "jqxHelpButton";
        var jqxHelpButtonType = "button";
        var jqxHelpButton = document.createElement("BUTTON");
		jqxHelpButton.type = jqxHelpButtonType;
        jqxHelpButton.id = id;
        jqxHelpButton.style = "float:left;";
        var text = document.createTextNode("Help");
        jqxHelpButton.appendChild(text);
        $("ul.jqx-menu-ul")[0].appendChild(jqxHelpButton);
        
        $("#jqxHelpButton").jqxButton({ 
            theme: "metro",
            width: 80, 
            height: 25,
            textImageRelation: "imageBeforeText", 
            textPosition: "left", 
            imgSrc: "scripts/HelpController/images/help_outline.png"
        });
        
        $("#jqxHelpButton").on('click', function (){
            
            var cssLink = document.createElement("link");
            cssLink.type = "text/css";
            cssLink.rel = "stylesheet";
            cssLink.href = "scripts/HelpController/helpPopup.css";
            document.getElementsByTagName("head")[0].appendChild(cssLink);
                
            $.getScript("scripts/HelpController/helpControllerVariantMetaphorCityoriginal.js", function(){   
                
                var helpPopupUl = helpControllerVariantMetaphorCityoriginal.helpControllerVarianthelpPopupUl();
                var legend_Cityoriginal = helpControllerVariantMetaphorCityoriginal.helpControllerVariantlegend_Cityoriginal();
                var relationships_Cityoriginal = helpControllerVariantMetaphorCityoriginal.helpControllerVariantrelationships_Cityoriginal();
                var navigation_Aframe = helpControllerVariantMetaphorCityoriginal.helpControllerVariantnavigation_Aframe();
                var navigation_x3dom = helpControllerVariantMetaphorCityoriginal.helpControllerVariantnavigation_x3dom ();
                
                var helpPopup_Navigation;
                if (visMode.includes( "x3dom")) {
                    helpPopup_Navigation = navigation_x3dom;
                    } else{
                    helpPopup_Navigation = navigation_Aframe;
                };
                if(controllerConfig.metaphor=="City original") {
                    var helpPopup = "<div id='legendTabs'>" + helpPopupUl + legend_Cityoriginal + relationships_Cityoriginal + helpPopup_Navigation + "</div>";
                    $("#DisplayWindow").remove();
                    var loadPopup = application.createPopup("Help",  
                    helpPopup, "DisplayWindow");
                    document.body.appendChild(loadPopup);
                    $("#DisplayWindow").css("display", "block").jqxWindow({
                            theme: "metro",
                            width: 565,
                            height: 650,
                            isModal: true,
                            autoOpen: true,
                            resizable: false
                    });
                        
                    $("#legendTabs").jqxTabs({ 
                        theme:'metro',
                        width: 550,
                        height: 600,
                        position:'top'
                    });
                };
           }); 
         
           $.getScript("scripts/HelpController/helpControllerVariantMetaphorCitybricks.js", function(){  
           
                var helpPopupUl = helpControllerVariantMetaphorCitybricks.helpControllerVarianthelpPopupUl();
                var legend_Citybricks = helpControllerVariantMetaphorCitybricks.helpControllerVariantlegend_Citybricks();
                var relationships_Citybricks = helpControllerVariantMetaphorCitybricks.helpControllerVariantrelationships_Citybricks();
                var navigation_Aframe = helpControllerVariantMetaphorCitybricks.helpControllerVariantnavigation_Aframe();
                var navigation_x3dom = helpControllerVariantMetaphorCitybricks.helpControllerVariantnavigation_x3dom ();
                
                var helpPopup_Navigation;
                if (visMode.includes( "x3dom")) {
                    helpPopup_Navigation = navigation_x3dom;
                    } else{
                    helpPopup_Navigation = navigation_Aframe;
                };
                if(controllerConfig.metaphor=="City bricks") {
                    var helpPopup = "<div id='legendTabs'>" + helpPopupUl + legend_Citybricks + relationships_Citybricks + helpPopup_Navigation + "</div>";
                    $("#DisplayWindow").remove();
                        var loadPopup = application.createPopup("Help",  
                        helpPopup, "DisplayWindow");
                        document.body.appendChild(loadPopup);
                        $("#DisplayWindow").css("display", "block").jqxWindow({
                                theme: "metro",
                                width: 565,
                                height: 650,
                                isModal: true,
                                autoOpen: true,
                                resizable: false
                    });
                    
                    $("#legendTabs").jqxTabs({ 
                        theme:'metro',
                        width: 550,
                        height: 600,
                        position:'top'
                    });
                };
          });  
        
          $.getScript("scripts/HelpController/helpControllerVariantMetaphorCityfloor.js", function(){  
            
                var helpPopupUl = helpControllerVariantMetaphorCityfloor.helpControllerVarianthelpPopupUl();
                var legend_Cityfloor = helpControllerVariantMetaphorCityfloor.helpControllerVariantlegend_Cityfloor();
                var relationships_Cityfloor = helpControllerVariantMetaphorCityfloor.helpControllerVariantrelationships_Cityfloor();
                var navigation_Aframe = helpControllerVariantMetaphorCityfloor.helpControllerVariantnavigation_Aframe();
                var navigation_x3dom = helpControllerVariantMetaphorCityfloor.helpControllerVariantnavigation_x3dom ();
                
                var helpPopup_Navigation;
                if (visMode.includes( "x3dom")) {
                    helpPopup_Navigation = navigation_x3dom;
                    } else{
                    helpPopup_Navigation = navigation_Aframe;
                };
                if(controllerConfig.metaphor=="City floor") {
                    var helpPopup = "<div id='legendTabs'>" + helpPopupUl + legend_Cityfloor + relationships_Cityfloor + helpPopup_Navigation + "</div>";
                    $("#DisplayWindow").remove();
                    var loadPopup = application.createPopup("Help",  
                    helpPopup, "DisplayWindow");
                    document.body.appendChild(loadPopup);
                    $("#DisplayWindow").css("display", "block").jqxWindow({
                            theme: "metro",
                            width: 565,
                            height: 650,
                            isModal: true,
                            autoOpen: true,
                            resizable: false
                    });
                        
                    $("#legendTabs").jqxTabs({ 
                        theme:'metro',
                        width: 550,
                        height: 600,
                        position:'top'
                    });
                };
            
         }); 
        
         $.getScript("scripts/HelpController/helpControllerVariantMetaphorRD.js", function(){
            
                var helpPopupUl = helpControllerVariantMetaphorRD.helpControllerVarianthelpPopupUl();  
                var legend_RD = helpControllerVariantMetaphorRD.helpControllerVariantlegend_RD();
                var relationships_RD = helpControllerVariantMetaphorRD.helpControllerVariantrelationships_RD();
                var navigation_Aframe = helpControllerVariantMetaphorRD.helpControllerVariantnavigation_Aframe();
                var navigation_x3dom = helpControllerVariantMetaphorRD.helpControllerVariantnavigation_x3dom ();
                var helpPopup_Navigation;
                if (visMode.includes( "x3dom")) {
                    helpPopup_Navigation = navigation_x3dom;
                    } else{
                    helpPopup_Navigation = navigation_Aframe;
                };
                
                if(controllerConfig.metaphor=="RD") {
                    var helpPopup = "<div id='legendTabs'>" + helpPopupUl + legend_RD + relationships_RD + helpPopup_Navigation + "</div>";
                    $("#DisplayWindow").remove();
                    var loadPopup = application.createPopup("Help",  
                    helpPopup, "DisplayWindow");
                    document.body.appendChild(loadPopup);
                    $("#DisplayWindow").css("display", "block").jqxWindow({
                            theme: "metro",
                            width: 565,
                            height: 650,
                            isModal: true,
                            autoOpen: true,
                            resizable: false
                    });
                        
                    $("#legendTabs").jqxTabs({ 
                        theme:'metro',
                        width: 550,
                        height: 600,
                        position:'top'
                    });
                };
         });
        
        });
	}
	
    return {
        initialize: initialize,
		activate: activate
	};    
})();

