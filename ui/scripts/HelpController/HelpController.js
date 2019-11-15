var helpController = (function() {
    let controllerConfig = {
        Metaphor: "",
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
    
            var legendTabsUl = "<ul style='font-size: 15px;'><li><img src='scripts/HelpController/images/category.png' width='25px' height= '25px' style='margin-right:10px;'>Legend</li><li><img src='scripts/HelpController/images/mouse.png' width='25px' height= '25px' style='margin-right:10px;'>Navigation</li></ul>";

            var legendTabsUl_City = "<ul style='font-size: 15px;'><li ><img src='scripts/HelpController/images/category.png' width='25px' height= '25px' style='margin-right:10px;'>Legend</li><li><img src='scripts/HelpController/images/group_work.png' width='25px' height= '25px' style='margin-right:10px;'>Relationships</li><li><img src='scripts/HelpController/images/mouse.png' width='25px' height= '25px' style='margin-right:10px;'>Navigation</li></ul>";
            
            var legendPopupUl_Mouse_City_Div = "<div id='legendPopupUl_Mouse_City_Div'><ul id='legendPopupUl_Mouse_City' class='Ul_Navigation' style='list-style-type: none;font-size: 15px;'><li class='legendPopupli' style='padding:10px;'><img src='scripts/Legend/images/left.png' class='navigation_Img' width='17px' height= '30px' style='margin-right:10px;'>Rotate</li><li class='legendPopupli' style='padding:10px;'><img src='scripts/Legend/images/middle.png' class='navigation_Img' width='17px' height= '30px' style='margin-right:10px;'>Move</li><li class='legendPopupli' style='padding:10px;'><img src='scripts/Legend/images/scrolling.png' class='navigation_Img' width='17px' height= '30px' style='margin-right:10px;'>Zoom</li></ul></div>";
        
            var legendPopupUl_Mouse_Div = "<div id='legendPopupUl_Mouse_Div'><ul id='legendPopupUl_Mouse' class='Ul_Navigation' style='list-style-type: none;font-size: 15px;' ><li class='legendPopupli' style='padding:10px;'><img src='scripts/Legend/images/left.png' class='navigation_Img' width='17px' height= '30px' style='margin-right:10px;'>Rotate</li><li class='legendPopupli' style='padding:10px;'><img src='scripts/Legend/images/double.png' class='navigation_Img' width='17px' height= '30px'style='margin-right:10px;'>Center</li><li class='legendPopupli' style='padding:10px;'><img src='scripts/Legend/images/middle.png' class='navigation_Img' width='17px' height= '30px' style='margin-right:10px;'>Move</li><li class='legendPopupli' style='padding:10px;'><img src='scripts/Legend/images/scrolling.png' class='navigation_Img' width='17px' height= '30px' style='margin-right:10px;'>Zoom</li></ul></div>";
            
            var legendPopupUl_City_Div = "<div id='legendPopupUl_City_Div'><p style='padding: 10px; text-align: justify;'>The city metaphor is a 3 dimensional real-world metaphor, aimed at creating a better understanding of the visualized system through a natural environment. The packages of an analysed system are represented by districts, its classes by Buildings. Methods and Attributes as well as different metrics of this metaphor differ depending on the chosen version which can either be original, panels, bricks or floors.</p><br/><img src='scripts/HelpController/images/city_legend.PNG' style='padding: 50px;'></div>";
            var legendPopupUl_Div = "<div id='legendPopupUl_Div'><p style='padding: 10px; text-align: justify;'>The Recursive Disk (RD) metaphor is a glyph-based approach for software visualization. In this case a glyph is a graphical entity with components, each of which has geometric and appearance attributes. Due to the simple visual appearance of this metaphor it is easy for stakeholders to get a good understanding of the different metaphor entities. Packages are represented by grey disks, which can contain inner packages as well. Classes are visualized by purple disks and can contain methods and attributes. Depending on the chosen variant these attributes and methods can be represented by disks or disk segments. Though they are distinguishable by their color, methods are blue and attributes a yellow.</p><br/><img src='scripts/HelpController/images/RD_legend.PNG' style='margin-top=30px;'></div>";
            
            var legendPopupUl_Relationships_City_Div = "<div id='legendPopupUl_City_Div'><img src='scripts/HelpController/images/inheritance.PNG' ></div>";
            
            
            var helpPopup_Navigation;
            var helpPopup_Legend;
            var legendPopup;
            
            if (visMode.includes( "x3dom")) {
                helpPopup_Navigation = legendPopupUl_Mouse_Div;
                } else{
                helpPopup_Navigation = legendPopupUl_Mouse_City_Div;
            };
            
            if(controllerConfig.Metaphor=="city") {
                helpPopup_Legend = legendPopupUl_City_Div ;
                legendPopup = "<div id='legendTabs'>" + legendTabsUl_City + helpPopup_Legend + legendPopupUl_Relationships_City_Div+ helpPopup_Navigation + "</div>";
            }; 
            if(controllerConfig.Metaphor=="RD") {
                helpPopup_Legend = legendPopupUl_Div ; 
                legendPopup = "<div id='legendTabs'>" + legendTabsUl + helpPopup_Legend + helpPopup_Navigation + "</div>";
            }; 
            
            
            
            $("#DisplayWindow").remove();
            var loadPopup = application.createPopup("Help",  
            legendPopup, "DisplayWindow");
            document.body.appendChild(loadPopup);
            $("#DisplayWindow").css("display", "block").jqxWindow({
                    theme: "metro",
                    width: 545,
                    height: 650,
                    isModal: true,
                    autoOpen: true,
                    resizable: false
            });

            $("#legendTabs").jqxTabs({ 
                theme:'metro',
                width: 530,
                height: 600,
                position:'top'
            });
        });
	}
	
    return {
        initialize: initialize,
		activate: activate
	};    
})();

