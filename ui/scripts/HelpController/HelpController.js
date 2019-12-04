var helpController = (function() {
    let controllerConfig = {
        metaphor: "",
	};
    
    function initialize(setupConfig){   
        
        application.transferConfigParams(setupConfig, controllerConfig);
    }
    
    function createPopup(){
        var helpPopupDiv= "<div id='helpPopupTabs'></div>";
        $("#DisplayWindow").remove();
        var loadPopup = application.createPopup("Help",  
                        helpPopupDiv, "DisplayWindow");
                        document.body.appendChild(loadPopup);
        $("#DisplayWindow").css("display", "block").jqxWindow({
                                theme: "metro",
                                width: 565,
                                height: 650,
                                isModal: true,
                                autoOpen: true,
                                resizable: false
        });
        return loadPopup; 
    };
    
    function createTabs(){
         $("#legendTabs").jqxTabs({ 
                    theme:'metro',
                    width: 550,
                    height: 600,
                    position:'top'
        });
    };
    
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
            
        /*if(controllerConfig.metaphor=="City original") {
            $.getScript("scripts/HelpController/variantMetaphorCityoriginal.js", function(){   
                var tabsUl = variantMetaphorCityoriginal.tabsUl();
                var legend_Cityoriginal = variantMetaphorCityoriginal.legend();
                var relationships_Cityoriginal = variantMetaphorCityoriginal.relationships();
                var helpPopup_Navigation = variantMetaphorCityoriginal.navigation();
                var helpPopup = "<div id='legendTabs'>" + tabsUl + legend_Cityoriginal + relationships_Cityoriginal + helpPopup_Navigation + "</div>";
                createPopup();
                document.getElementById('helpPopupTabs').innerHTML= helpPopup;
                createTabs(); 
            });
         };
    
        if(controllerConfig.metaphor=="City bricks") {
           $.getScript("scripts/HelpController/variantMetaphorCitybricks.js", function(){  
                var tabsUl = variantMetaphorCitybricks.tabsUl();
                var legend_Citybricks = variantMetaphorCitybricks.legend();
                var relationships_Citybricks = variantMetaphorCitybricks.relationships();
                var helpPopup_Navigation = variantMetaphorCitybricks.navigation();
                var helpPopup = "<div id='legendTabs'>" + tabsUl + legend_Citybricks + relationships_Citybricks + helpPopup_Navigation + "</div>";
                createPopup();
                document.getElementById('helpPopupTabs').innerHTML= helpPopup;
                createTabs(); 
            }); 
        };
        
        if(controllerConfig.metaphor=="City floor") {
            $.getScript("scripts/HelpController/variantMetaphorCityfloor.js", function(){  
                var tabsUl = variantMetaphorCityfloor.tabsUl();
                var legend_Cityfloor = variantMetaphorCityfloor.legend();
                var relationships_Cityfloor = variantMetaphorCityfloor.relationships();
                var navigation = variantMetaphorCityfloor.navigation();
                var helpPopup = "<div id='legendTabs'>" + tabsUl + legend_Cityfloor + relationships_Cityfloor +  navigation + "</div>";
                createPopup();
                document.getElementById('helpPopupTabs').innerHTML= helpPopup;
                createTabs(); 
            }); 
        };
        
        if(controllerConfig.metaphor=="RD") {
            $.getScript("scripts/HelpController/variantMetaphorRD.js", function(){
                var tabsUl = variantMetaphorRD.tabsUl();  
                var legend_RD = variantMetaphorRD.legend();
                var relationships_RD = variantMetaphorRD.relationships();
                var navigation = variantMetaphorRD.navigation();
    
                var helpPopup = "<div id='legendTabs'>" + tabsUl + legend_RD + relationships_RD +  navigation + "</div>";
                createPopup();
                document.getElementById('helpPopupTabs').innerHTML= helpPopup;
                createTabs(); 
           });
        };*/
        
        if(controllerConfig.metaphor=="City original") {
            $.getScript("scripts/HelpController/variantMetaphorCityoriginal.js", function(){   
                var helpPopup = "<div id='legendTabs'>" + variantMetaphorCityoriginal.tabsUl() + variantMetaphorCityoriginal.legend() + variantMetaphorCityoriginal.relationships() + variantMetaphorCityoriginal.navigation() + "</div>";
                
                createPopup();
                document.getElementById('helpPopupTabs').innerHTML= helpPopup;
                createTabs(); 
            });
        };
        
        if(controllerConfig.metaphor=="City bricks") {
           $.getScript("scripts/HelpController/variantMetaphorCitybricks.js", function(){  
                var helpPopup = "<div id='legendTabs'>" + variantMetaphorCitybricks.tabsUl() + variantMetaphorCitybricks.legend() + variantMetaphorCitybricks.relationships() + variantMetaphorCitybricks.navigation() + "</div>";
                
                createPopup();
                document.getElementById('helpPopupTabs').innerHTML= helpPopup;
                createTabs(); 
            }); 
        };
        
        if(controllerConfig.metaphor=="City floor") {
            $.getScript("scripts/HelpController/variantMetaphorCityfloor.js", function(){  
                var helpPopup = "<div id='legendTabs'>" + variantMetaphorCityfloor.tabsUl() + variantMetaphorCityfloor.legend() + variantMetaphorCityfloor.relationships() + variantMetaphorCityfloor.navigation() + "</div>";
                
                createPopup();
                document.getElementById('helpPopupTabs').innerHTML= helpPopup;
                createTabs(); 
            }); 
        };

        if(controllerConfig.metaphor=="RD") {
            $.getScript("scripts/HelpController/variantMetaphorRD.js", function(){
                var helpPopup = "<div id='legendTabs'>" + variantMetaphorRD.tabsUl() + variantMetaphorRD.legend() + variantMetaphorRD.relationships() + variantMetaphorRD.navigation() + "</div>";
                
                //console.log(helpControllerVarianthelpPopupUl);
                
                createPopup();
                document.getElementById('helpPopupTabs').innerHTML= helpPopup;
                createTabs(); 
           });
        };
        
        
      
        /*$.getScript("scripts/HelpController/variantFunction.js", function(){	 // 
    
            var helpPopupUl = variantFunction.helpPopupUl();
            var legend_Cityoriginal = variantFunction.legend_Cityoriginal();
            var relationships_Cityoriginal = variantFunction.relationships_Cityoriginal();
            var legend_Citybricks = variantFunction.legend_Citybricks();
            var relationships_Citybricks = variantFunction.relationships_Citybricks();
            var legend_Cityfloor = variantFunction.legend_Cityfloor();
            var relationships_Cityfloor = variantFunction.relationships_Cityfloor();
            var relationships_RD = variantFunction.relationships_RD();
            var legend_RD = variantFunction.legend_RD();
            var helpPopup_Navigation = variantFunction.navigation();
            
            if(controllerConfig.metaphor=="City original") {
                var helpPopup = "<div id='legendTabs'>" + helpPopupUl + legend_Cityoriginal + relationships_Cityoriginal + helpPopup_Navigation + "</div>";
            };
            
            if(controllerConfig.metaphor=="City bricks") {
                var helpPopup = "<div id='legendTabs'>" + helpPopupUl + legend_Citybricks + relationships_Citybricks + helpPopup_Navigation + "</div>";
            }; 
                
            if(controllerConfig.metaphor=="City floor") {
                var helpPopup = "<div id='legendTabs'>" + helpPopupUl + legend_Cityfloor + relationships_Cityfloor + helpPopup_Navigation + "</div>";
            };
            
            if(controllerConfig.metaphor=="RD") {
                var helpPopup = "<div id='legendTabs'>" + helpPopupUl + legend_RD + relationships_RD + helpPopup_Navigation + "</div>";
            }; 
            
            createPopup();
            document.getElementById('helpPopupTabs').innerHTML= helpPopup;
            createTabs();
        });*/
        
      });
    }
	
    return {
        initialize: initialize,
		activate: activate
	};    
})();

