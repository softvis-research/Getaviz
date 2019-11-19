var helpController = (function() {
    let controllerConfig = {
        Metaphor: "",
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
    
            var helpPopupUl = "<ul class='helpPopupUl'><li><div><img src='scripts/HelpController/images/category.png'><div class='helpPopup_Ul_div'>Legend</div></div></li><li><div><img src='scripts/HelpController/images/group_work.png'><div class='helpPopup_Ul_div'>Relationships</div></div></li><li><div><img src='scripts/HelpController/images/mouse.png'><div class='helpPopup_Ul_div'>Navigation</div></div></li></ul>";

            var helpPopupUl_City = "<ul class='helpPopupUl'><li><div><img src='scripts/HelpController/images/category.png'><div class='helpPopup_Ul_div'>Legend</div></div></li><li><div><img src='scripts/HelpController/images/group_work.png'><div class='helpPopup_Ul_div'>Relationships</div></div></li><li><div><img src='scripts/HelpController/images/mouse.png'><div class='helpPopup_Ul_div'>Navigation</div></div></li></ul>";
            
            var navigation_City_Div = "<div><ul class='Ul_Navigation'><li class='navigationLi'><img src='scripts/Legend/images/left.png'>Rotate</li><li class='navigationLi'><img src='scripts/Legend/images/middle.png'>Move</li><li class='navigationLi'><img src='scripts/Legend/images/scrolling.png' >Zoom</li></ul></div>";
        
            var navigation_Div = "<div><ul class='Ul_Navigation'><li class='navigationLi'><img src='scripts/Legend/images/left.png'>Rotate</li><li class='navigationLi'><img src='scripts/Legend/images/double.png'>Center</li><li class='navigationLi'><img src='scripts/Legend/images/middle.png'>Move</li><li class='navigationLi'><img src='scripts/Legend/images/scrolling.png'>Zoom</li></ul></div>";
            
            var legend_City_Div = "<div class='legend_City_Div'><p>The city metaphor is a 3 dimensional real-world metaphor, aimed at creating a better understanding of the visualized system through a natural environment. The packages of an analysed system are represented by districts, its classes by Buildings. Methods and Attributes as well as different metrics of this metaphor differ depending on the chosen version which can either be original, panels, bricks or floors.</p><br/><img src='scripts/HelpController/images/city_legend.PNG'></div>";
            
            var legend_Div = "<div class='legend_Div'><p>The Recursive Disk (RD) metaphor is a glyph-based approach for software visualization. In this case a glyph is a graphical entity with components, each of which has geometric and appearance attributes. Due to the simple visual appearance of this metaphor it is easy for stakeholders to get a good understanding of the different metaphor entities. Packages are represented by grey disks, which can contain inner packages as well. Classes are visualized by purple disks and can contain methods and attributes. Depending on the chosen variant these attributes and methods can be represented by disks or disk segments. Though they are distinguishable by their color, methods are blue and attributes a yellow.</p><img src='scripts/HelpController/images/RD_legend.PNG'></div>";
            
            var relationships_City_Div= "<div class='relationships_City_Div'><img src='scripts/HelpController/images/inheritance.PNG' ></div>";
            var relationships_RD_Div= "<div class='relationships_RD_Div'><h3>Inheritance:</h3><img src='scripts/HelpController/images/abstractProduct.PNG' ><h3>Field accesses:</h3><img src='scripts/HelpController/images/voidBusiness.PNG' ><h3>Method call:</h3><img src='scripts/HelpController/images/voidRun.PNG' ></div>";
            
            
            var helpPopup_Navigation;
            var helpPopup_Legend;
            var helpPopup;
            
            if (visMode.includes( "x3dom")) {
                helpPopup_Navigation = navigation_Div;
                } else{
                helpPopup_Navigation = navigation_City_Div;
            };
            
            if(controllerConfig.Metaphor=="city") {
                helpPopup_Legend = legend_City_Div ;
                helpPopup = "<div id='legendTabs'>" + helpPopupUl_City + helpPopup_Legend + relationships_City_Div + helpPopup_Navigation + "</div>";
            }; 
            if(controllerConfig.Metaphor=="RD") {
                helpPopup_Legend = legend_Div ; 
                helpPopup = "<div id='legendTabs'>" + helpPopupUl + helpPopup_Legend + relationships_RD_Div + helpPopup_Navigation + "</div>";
            }; 
            
            
            
            $("#DisplayWindow").remove();
            var loadPopup = application.createPopup("Help",  
            helpPopup, "DisplayWindow");
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

