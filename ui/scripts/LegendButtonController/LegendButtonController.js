var legendButtonController = (function() {
    
    function initialize(){
		
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
            
            var legendPopupDiv = "<div id='legendPopupDiv'><div>";
            var legendPopupUl = "<ul id='legendPopupUl' style='list-style-type: none;'><li><img src='scripts/Legend/images/circle_gray.png' >Package</li><li><img src='scripts/Legend/images/circle_blue.png' >Type</li><li><img src='scripts/Legend/images/circle_blue_light.png' >Method</li><li><img src='scripts/Legend/images/circle_yellow.png' >Field</li><li><img src='scripts/Legend/images/navigation.png' width='30px' height= '30px' >Navigation</li></ul>";
            var legendPopupUl_Navigation = "<ul id='legendPopupUl_Navigation'  style='list-style-type: none; margin-left:10px;' ><li class='legendPopupli'><img src='scripts/Legend/images/left.png' width='17px' height= '30px' list-style-type: none;>Rotate</li><li class='legendPopupli'><img src='scripts/Legend/images/double.png' width='17px' height= '30px'>Center</li><li class='legendPopupli' ><img src='scripts/Legend/images/middle.png' width='17px' height= '30px'>Move</li><li class='legendPopupli'><img src='scripts/Legend/images/scrolling.png' width='17px' height= '30px'>Zoom</li></ul>";
            
           $(".legendPopupli").style="margin: 5px;border-style: solid; width:300px; height:300px;";
           
            var legendPopup = legendPopupUl + legendPopupUl_Navigation ;
            $("#DisplayWindow").remove();
            var loadPopup = application.createPopup("Legend",  
            legendPopup, "DisplayWindow");
            document.body.appendChild(loadPopup);
            $("#DisplayWindow").css("display", "block").jqxWindow({
                    theme: "metro",
                    width: 400,
                    height: 500,
                    isModal: true,
                    autoOpen: true,
                    resizable: false
            });
        });

	}

    

    return {
        initialize: initialize,
		activate: activate
		//reset: reset
	};    
})();

