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
            
            createPopup();
            createTabs();
       });
    }

    function createPopup(){
        $("#DisplayWindow").remove();
        var popup = application.createPopup("Help",  createJqxTabsHTML(), "DisplayWindow");
        document.body.appendChild(popup);
        $("#DisplayWindow").css("display", "block").jqxWindow({
            theme: "metro",
            width: 565,
            height: 645,
            isModal: true,
            autoOpen: true,
            resizable: false
        });
    };
    
    function createTabs(){
        $('#helpPopupTabs').jqxTabs({ 
            theme:'metro',
            width: 550,
            height: 600,
            position:'top'
        });

        if(controllerConfig.metaphor=="City original") {
            $.getScript("scripts/HelpController/metaphorCityoriginal.js", function(){   
                createPopupContent()
            });
        };
            
        if(controllerConfig.metaphor=="City bricks") {
            $.getScript("scripts/HelpController/metaphorCitybricks.js", function(){  
                createPopupContent()
            });
        };
            
        if(controllerConfig.metaphor=="City floor") {
            $.getScript("scripts/HelpController/metaphorCityfloor.js", function(){  
                createPopupContent()
            });
        };

        if(controllerConfig.metaphor=="RD") {
            $.getScript("scripts/HelpController/metaphorRD.js", function(){  
                createPopupContent()
            }); 
        };
    };
    
    function createPopupContent(){
        $('#helpPopupTabs').jqxTabs('setContentAt', 0, variantMetaphor.legend());
        $('#helpPopupTabs').jqxTabs('setContentAt', 1, variantMetaphor.relationships());
        $.getScript("scripts/HelpController/navigationContent.js", function(){ 
            $('#helpPopupTabs').jqxTabs('setContentAt', 2, variantNavigation.navigation());   
        });
    };

    function createJqxTabsHTML(){
        return `
            <div id='helpPopupTabs'>
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
                <div></div>
            </div>`;
    };
    
    return {
        initialize: initialize,
		activate: activate
	};    
})();

