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
                
           $.getScript("scripts/HelpController/helpControllerVariantFunction.js", function(){	 // 
    
            var helpPopup_Navigation;
            var helpPopup_Legend;
            var helpPopup;
            
 
            var helpPopupUl = helpControllerVariantFunction.helpControllerVarianthelpPopupUl();
            var legend_Cityoriginal = helpControllerVariantFunction.helpControllerVariantlegend_Cityoriginal();
            var relationships_Cityoriginal = helpControllerVariantFunction.helpControllerVariantrelationships_Cityoriginal();
            var legend_Citybricks = helpControllerVariantFunction.helpControllerVariantlegend_Citybricks();
            var relationships_Citybricks = helpControllerVariantFunction.helpControllerVariantrelationships_Citybricks();
            var legend_Cityfloor = helpControllerVariantFunction.helpControllerVariantlegend_Cityfloor();
            var relationships_Cityfloor = helpControllerVariantFunction.helpControllerVariantrelationships_Cityfloor();
            var relationships_RD = helpControllerVariantFunction.helpControllerVariantrelationships_RD();
            var legend_RD = helpControllerVariantFunction.helpControllerVariantlegend_RD();
            var navigation_Aframe = helpControllerVariantFunction.helpControllerVariantnavigation_Aframe();
            var navigation_x3dom = helpControllerVariantFunction.helpControllerVariantnavigation_x3dom ();
            
            console.log(navigation_Aframe);
            if (visMode.includes( "x3dom")) {
                helpPopup_Navigation = navigation_x3dom;
                } else{
                helpPopup_Navigation = navigation_Aframe;
            };
            
            if(controllerConfig.metaphor=="City original") {
                helpPopup_Legend = legend_Cityoriginal;
                helpPopup = "<div id='legendTabs'>" + helpPopupUl + helpPopup_Legend + relationships_Cityoriginal + helpPopup_Navigation + "</div>";
            };
            
            if(controllerConfig.metaphor=="City bricks") {
                helpPopup_Legend = legend_Citybricks;
                helpPopup = "<div id='legendTabs'>" + helpPopupUl + helpPopup_Legend + relationships_Citybricks + helpPopup_Navigation + "</div>";
             }; 
                
            if(controllerConfig.metaphor=="City floor") {
                helpPopup_Legend = legend_Cityfloor;
                helpPopup = "<div id='legendTabs'>" + helpPopupUl + helpPopup_Legend + relationships_Cityfloor + helpPopup_Navigation + "</div>";
             };
            
            if(controllerConfig.metaphor=="RD") {
                helpPopup_Legend = legend_RD ; 
                helpPopup = "<div id='legendTabs'>" + helpPopupUl + helpPopup_Legend + relationships_RD + helpPopup_Navigation + "</div>";
            }; 
            
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
           });
        });
	}
	
    return {
        initialize: initialize,
		activate: activate
	};    
})();

