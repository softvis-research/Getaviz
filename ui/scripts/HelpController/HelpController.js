var helpController = (function() {
    let controllerConfig = {
        metaphor: "",
	};
    
    function initialize(setupConfig){   
        
        application.transferConfigParams(setupConfig, controllerConfig);
    }
    
    function helpControllerPopup(){
        var helpPopup= "<div id='helpPopupTabs'></div>";
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
        return loadPopup; 
    };
    
    function helpControllerJqxTabs(){
        return $("#legendTabs").jqxTabs({ 
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
            
            $.getScript("scripts/HelpController/helpControllerVariantMetaphorCityoriginal.js", function(){   
                
                var helpPopupUl = helpControllerVariantMetaphorCityoriginal.helpControllerVarianthelpPopupUl();
                var legend_Cityoriginal = helpControllerVariantMetaphorCityoriginal.helpControllerVariantlegend_Cityoriginal();
                var relationships_Cityoriginal = helpControllerVariantMetaphorCityoriginal.helpControllerVariantrelationships_Cityoriginal();
                var helpPopup_Navigation = helpControllerVariantMetaphorCityoriginal.helpControllerNavigation();
  
                if(controllerConfig.metaphor=="City original") {
                    var helpPopup = "<div id='legendTabs'>" + helpPopupUl + legend_Cityoriginal + relationships_Cityoriginal + helpPopup_Navigation + "</div>";
                    helpControllerPopup.call();
                    document.getElementById('helpPopupTabs').innerHTML= helpPopup;
                    helpControllerJqxTabs.call(); 
                };
           }); 
         
           $.getScript("scripts/HelpController/helpControllerVariantMetaphorCitybricks.js", function(){  
           
                var helpPopupUl = helpControllerVariantMetaphorCitybricks.helpControllerVarianthelpPopupUl();
                var legend_Citybricks = helpControllerVariantMetaphorCitybricks.helpControllerVariantlegend_Citybricks();
                var relationships_Citybricks = helpControllerVariantMetaphorCitybricks.helpControllerVariantrelationships_Citybricks();
                var helpPopup_Navigation = helpControllerVariantMetaphorCitybricks.helpControllerNavigation();
                
                if(controllerConfig.metaphor=="City bricks") {
                    var helpPopup = "<div id='legendTabs'>" + helpPopupUl + legend_Citybricks + relationships_Citybricks + helpPopup_Navigation + "</div>";
                    helpControllerPopup.call();
                    document.getElementById('helpPopupTabs').innerHTML= helpPopup;
                    helpControllerJqxTabs.call(); 
                };
          });  
        
          $.getScript("scripts/HelpController/helpControllerVariantMetaphorCityfloor.js", function(){  
            
                var helpPopupUl = helpControllerVariantMetaphorCityfloor.helpControllerVarianthelpPopupUl();
                var legend_Cityfloor = helpControllerVariantMetaphorCityfloor.helpControllerVariantlegend_Cityfloor();
                var relationships_Cityfloor = helpControllerVariantMetaphorCityfloor.helpControllerVariantrelationships_Cityfloor();
                var helpPopup_Navigation = helpControllerVariantMetaphorCityfloor.helpControllerNavigation();
     
                if(controllerConfig.metaphor=="City floor") {
                    var helpPopup = "<div id='legendTabs'>" + helpPopupUl + legend_Cityfloor + relationships_Cityfloor + helpPopup_Navigation + "</div>";
                    helpControllerPopup.call();
                    document.getElementById('helpPopupTabs').innerHTML= helpPopup;
                    helpControllerJqxTabs.call(); 
                };
         }); 
        
         $.getScript("scripts/HelpController/helpControllerVariantMetaphorRD.js", function(){
            
                var helpPopupUl = helpControllerVariantMetaphorRD.helpControllerVarianthelpPopupUl();  
                var legend_RD = helpControllerVariantMetaphorRD.helpControllerVariantlegend_RD();
                var relationships_RD = helpControllerVariantMetaphorRD.helpControllerVariantrelationships_RD();
                var helpPopup_Navigation = helpControllerVariantMetaphorRD.helpControllerNavigation(); //
                
                if(controllerConfig.metaphor=="RD") {
                    var helpPopup = "<div id='legendTabs'>" + helpPopupUl + legend_RD + relationships_RD + helpPopup_Navigation + "</div>";
                    helpControllerPopup.call();
                    document.getElementById('helpPopupTabs').innerHTML= helpPopup;
                    helpControllerJqxTabs.call(); 
                 };
         });
        
      });
    }
	
    return {
        initialize: initialize,
		activate: activate
	};    
})();

