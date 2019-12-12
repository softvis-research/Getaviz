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
            
            if(controllerConfig.metaphor=="City original") {
                $.getScript("scripts/HelpController/metaphorCityoriginal.js", function(){   
                     helpPopupContent();
                });
            };
            
            if(controllerConfig.metaphor=="City bricks") {
                $.getScript("scripts/HelpController/metaphorCitybricks.js", function(){  
                    helpPopupContent();
                }); 
            };
            
            if(controllerConfig.metaphor=="City floor") {
                $.getScript("scripts/HelpController/metaphorCityfloor.js", function(){  
                   helpPopupContent();
                }); 
            };

            if(controllerConfig.metaphor=="RD") {
                $.getScript("scripts/HelpController/metaphorRD.js", function(){  
                   helpPopupContent();
                }); 
            };
       });
    }
    
    function jqxTabsPopup(){
            var jqxTabsPopup = `
             <ul class='tabsUl helpController'>
                <li>
                     <div>
                        <img src='scripts/HelpController/images/category.png'>
                        <div class='tabsUl_div helpController'>Legend</div>
                     </div>
                </li>
                <li>
                     <div>
                         <img src='scripts/HelpController/images/group_work.png'>
                         <div class='tabsUl_div helpController'>Relationships</div>
                     </div>
                 </li>
                 <li>
                     <div >
                         <img src='scripts/HelpController/images/mouse.png'>
                         <div class='tabsUl_div helpController'>Navigation</div>
                     </div>
                 </li>
             </ul>
             <div></div>
             <div></div>
             <div></div>`;
            
         return jqxTabsPopup;     
    }; 

    function createPopup(){
        var helpPopupDiv= "<div id='helpPopupDiv'></div>";
        $("#DisplayWindow").remove();
        var loadPopup = application.createPopup("Help",  
                        helpPopupDiv, "DisplayWindow");
                        document.body.appendChild(loadPopup);
        $("#DisplayWindow").css("display", "block").jqxWindow({
                                theme: "metro",
                                width: 565,
                                height: 645,
                                isModal: true,
                                autoOpen: true,
                                resizable: false
        });
        return loadPopup; 
    };
    
    function popupContent(){
        return "<div id='helpPopupTabs'>" + jqxTabsPopup() + "</div>";
    };
            
    function createTabs(){
        $('#helpPopupTabs').jqxTabs({ 
                    theme:'metro',
                    width: 550,
                    height: 600,
                    position:'top'
        });

        $('#helpPopupTabs').jqxTabs('setContentAt', 0, variantMetaphor.legend());
        $('#helpPopupTabs').jqxTabs('setContentAt', 1, variantMetaphor.relationships());
        $('#helpPopupTabs').jqxTabs('setContentAt', 2, variantNavigation.navigation());
        
    };

    function createPopupAndTabs(){
        createPopup();
        document.getElementById('helpPopupDiv').innerHTML= popupContent();
        createTabs();
    };
    
    function helpPopupContent(){
        $.getScript("scripts/HelpController/navigationContent.js", function(){
            createPopupAndTabs();
        });
    };
    
    return {
        initialize: initialize,
		activate: activate
	};    
})();

