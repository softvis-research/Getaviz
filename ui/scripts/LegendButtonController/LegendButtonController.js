var legendButtonController = (function() {
    let controllerConfig = {
        showCityLegend: false,
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
        var legendtext = document.createTextNode("Legend");
        jqxLegendButton.appendChild(legendtext);
        $("ul.jqx-menu-ul")[0].appendChild(jqxLegendButton);
        
        $("#jqxLegendButton").jqxButton({ 
            theme: "metro",
            width: 80, 
            height: 25,
            textImageRelation: "imageBeforeText", 
            textPosition: "left", 
            imgSrc: "scripts/LegendButtonController/images/category.png"
        });
        
        $("#jqxLegendButton").on('click', function (){
            
            var legendPopupUl_City = "<ul id='legendPopupUl' style='list-style-type: none;font-size: 15px;'><li style='padding:10px;'><img src='scripts/Legend/images/circle_gray.png' style='margin-right:10px;'>Package</li><li style='padding:10px;'><img src='scripts/Legend/images/circle_black.png' style='margin-right:10px;'>Type</li><li style='padding:10px;'><img src='scripts/Legend/images/navigation.png' width='30px' height= '30px' style='margin-right:10px;'>Navigation<ul id='legendPopupUl_Maus_City'  style='list-style-type: none;' ><li class='legendPopupli' style='padding:10px;'><img src='scripts/Legend/images/left.png' width='17px' height= '30px' style='margin-right:10px;'>Rotate</li><li class='legendPopupli' style='padding:10px;'><img src='scripts/Legend/images/middle.png' width='17px' height= '30px' style='margin-right:10px;'>Move</li><li class='legendPopupli' style='padding:10px;'><img src='scripts/Legend/images/scrolling.png' width='17px' height= '30px' style='margin-right:10px;'>Zoom</li></ul></li></ul>";
            
            var legendPopupUl = "<ul id='legendPopupUl' style='list-style-type: none;font-size: 15px;'><li style='padding:10px;'><img src='scripts/Legend/images/circle_gray.png' style='margin-right:10px;'>Package</li><li style='padding:10px;'><img src='scripts/Legend/images/circle_black.png' style='margin-right:10px;'>Type</li><li style='padding:10px;'><img src='scripts/Legend/images/circle_blue_light.png' style='margin-right:10px;'>Method</li><li style='padding:10px;'><img src='scripts/Legend/images/circle_yellow.png' style='margin-right:10px;'>Field</li><li style='padding:10px;'><img src='scripts/Legend/images/navigation.png' style='margin-right:10px;' width='30px' height= '30px' >Navigation<ul id='legendPopupUl_Maus'  style='list-style-type: none;' ><li class='legendPopupli' style='padding:10px;'><img src='scripts/Legend/images/left.png' width='17px' height= '30px' style='margin-right:10px;'>Rotate</li><li class='legendPopupli' style='padding:10px;'><img src='scripts/Legend/images/double.png' width='17px' height= '30px'style='margin-right:10px;'>Center</li><li class='legendPopupli' style='padding:10px;'><img src='scripts/Legend/images/middle.png' width='17px' height= '30px' style='margin-right:10px;'>Move</li><li class='legendPopupli' style='padding:10px;'><img src='scripts/Legend/images/scrolling.png' width='17px' height= '30px' style='margin-right:10px;'>Zoom</li></ul></li></ul>";
            
            //document.getElementById("legendPopupli").style.color = "blue";
           
            var legendPopup;
            
            if(controllerConfig.showCityLegend===true) {
                legendPopup = legendPopupUl_City;
            } else{
                legendPopup = legendPopupUl;
            }; 
            
            $("#DisplayWindow").remove();
            var loadPopup = application.createPopup("Legend",  
            legendPopup, "DisplayWindow");
            document.body.appendChild(loadPopup);
            $("#DisplayWindow").css("display", "block").jqxWindow({
                    theme: "metro",
                    width: 350,
                    height: 580,
                    isModal: true,
                    autoOpen: true,
                    resizable: false
            });
        });
	}
	
    return {
        initialize: initialize,
		activate: activate
	};    
})();

