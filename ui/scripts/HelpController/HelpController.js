var helpController = (function() {
    let controllerConfig = {
        showCityLegend: "",
	};
    
    function initialize(setupConfig){   
        
        application.transferConfigParams(setupConfig, controllerConfig);
    }
	
	function activate(){
    
        var legendId = "jqxLegendButton";
        var legendButtonType = "button";
        var jqxLegendButton = document.createElement("BUTTON");
		jqxLegendButton.type = legendButtonType;
        jqxLegendButton.id = legendId;
        jqxLegendButton.style = "float:left;";
        var legendtext = document.createTextNode("Help");
        jqxLegendButton.appendChild(legendtext);
        $("ul.jqx-menu-ul")[0].appendChild(jqxLegendButton);
        
        $("#jqxLegendButton").jqxButton({ 
            theme: "metro",
            width: 80, 
            height: 25,
            textImageRelation: "imageBeforeText", 
            textPosition: "left", 
            imgSrc: "scripts/HelpController/images/help_outline.png"
        });
        
        $("#jqxLegendButton").on('click', function (){
            
            var legendDiv = "<div id='legendDiv'></div>";
    
            var legendTabsUl = "<ul style='font-size: 15px;'><li style='margin-left: 45px;'><img src='scripts/HelpController/images/category.png' width='25px' height= '25px' style='margin-right:10px;'>Legend</li><li><img src='scripts/HelpController/images/mouse.png' width='25px' height= '25px' style='margin-right:10px;'>Navigation</li></ul>";
            
            var legendPopupUl_City_Div = "<div id='legendPopupUl_City_Div'><ul id='legendPopupUl' style='list-style-type: none;font-size: 15px;'><li style='padding:10px;'><img src='scripts/HelpController/images/package_city.png' style='margin-right:10px;' width='25px' height= '30px' >Package</li><li style='padding:10px;'><img src='scripts/HelpController/images/type_city.png' style='margin-right:10px;' width='25px' height= '30px'>Type</li></ul></div>";
            
            var legendPopupUl_Mouse_City_Div = "<div id='legendPopupUl_Mouse_City_Div'><ul id='legendPopupUl_Mouse_City'  style='list-style-type: none; font-size: 15px;' ><li class='legendPopupli' style='padding:10px;'><img src='scripts/Legend/images/left.png' width='17px' height= '30px' style='margin-right:10px;'>Rotate</li><li class='legendPopupli' style='padding:10px;'><img src='scripts/Legend/images/middle.png' width='17px' height= '30px' style='margin-right:10px;'>Move</li><li class='legendPopupli' style='padding:10px;'><img src='scripts/Legend/images/scrolling.png' width='17px' height= '30px' style='margin-right:10px;'>Zoom</li></ul></div>";
        
            var legendPopupUl_Div = "<div id='legendPopupUl_Div'><ul id='legendPopupUl' style='list-style-type: none;font-size: 15px;'><li style='padding:10px;'><img src='scripts/Legend/images/circle_gray.png' style='margin-right:10px;'>Package</li><li style='padding:10px;'><img src='scripts/Legend/images/circle_black.png' style='margin-right:10px;'>Type</li><li style='padding:10px;'><img src='scripts/Legend/images/circle_blue_light.png' style='margin-right:10px;'>Method</li><li style='padding:10px;'><img src='scripts/Legend/images/circle_yellow.png' style='margin-right:10px;'>Field</li></ul></div>";
            
            var legendPopupUl_Mouse_Div = "<div id='legendPopupUl_Mouse_Div'><ul id='legendPopupUl_Mouse'  style='list-style-type: none; font-size: 15px;' ><li class='legendPopupli' style='padding:10px;'><img src='scripts/Legend/images/left.png' width='17px' height= '30px' style='margin-right:10px;'>Rotate</li><li class='legendPopupli' style='padding:10px;'><img src='scripts/Legend/images/double.png' width='17px' height= '30px'style='margin-right:10px;'>Center</li><li class='legendPopupli' style='padding:10px;'><img src='scripts/Legend/images/middle.png' width='17px' height= '30px' style='margin-right:10px;'>Move</li><li class='legendPopupli' style='padding:10px;'><img src='scripts/Legend/images/scrolling.png' width='17px' height= '30px' style='margin-right:10px;'>Zoom</li></ul></div>";
            
            var helpPopup_Navigation;
            var helpPopup_Legend;
            var legendPopup;
            
            if (visMode.includes( "x3dom")) {
                helpPopup_Navigation = legendPopupUl_Mouse_Div;
                } else{
                helpPopup_Navigation = legendPopupUl_Mouse_City_Div;
            };
            
            if(controllerConfig.showCityLegend=="city") {
                helpPopup_Legend = legendPopupUl_City_Div ;
            }; 
            if(controllerConfig.showCityLegend=="RD") {
                helpPopup_Legend  = legendPopupUl_Div ; 
            }; 
            
            legendPopup = "<div id='legendTabs'>" + legendTabsUl + helpPopup_Legend + helpPopup_Navigation + "</div>";
            
            $("#DisplayWindow").remove();
            var loadPopup = application.createPopup("Help",  
            legendPopup, "DisplayWindow");
            document.body.appendChild(loadPopup);
            $("#DisplayWindow").css("display", "block").jqxWindow({
                    theme: "metro",
                    width: 363,
                    height: 350,
                    isModal: true,
                    autoOpen: true,
                    resizable: false
            });

            $("#legendTabs").jqxTabs({ 
                theme:'metro',
                width:350,
                height: 300,
                position:'top'
            });
        });
	}
	
    return {
        initialize: initialize,
		activate: activate
	};    
})();

