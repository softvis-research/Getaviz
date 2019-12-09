var helpController = (function() {
    let controllerConfig = {
        metaphor: "",
	};
    
    function initialize(setupConfig){   
        
        application.transferConfigParams(setupConfig, controllerConfig);
    }
    
    function tabsUl(){
            var helpPopupUl = `
             <ul class='helpPopupUl helpController'>
                 <li>
                    <div>
                        <img src='scripts/HelpController/images/category.png'>
                         <div class='helpPopup_Ul_div helpController'>Legend</div>
                     </div>
                 </li>
                <li>
                     <div>
                         <img src='scripts/HelpController/images/group_work.png'>
                         <div class='helpPopup_Ul_div helpController'>Relationships</div>
                     </div>
                 </li>
                 <li>
                     <div>
                         <img src='scripts/HelpController/images/mouse.png'>
                         <div class='helpPopup_Ul_div helpController'>Navigation</div>
                     </div>
                 </li>
             </ul>`;
         return helpPopupUl;     
    }; 
    
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
    
    /*function popupConment(a,b){
        var helpPopup = "<div id='legendTabs'>" + a + b + "</div>";
        return helpPopup;
    };
    function divContent(){
         var content= variantMetaphor.legend() + variantMetaphor.relationships() + variantMetaphor.navigation();
         return content;
     };*/
    
    function popupConment(){
        var helpPopup = "<div id='legendTabs'>" + tabsUl() + variantMetaphor.legend() + variantMetaphor.relationships() + variantMetaphor.navigation() + "</div>";
        return helpPopup;
    };
    
    function createTabs(){
        
         $("#legendTabs").jqxTabs({ 
                    theme:'metro',
                    width: 550,
                    height: 600,
                    position:'top'
        });
    };

    function createPopupTabs(helpPopup){
         
          createPopup();
          document.getElementById('helpPopupTabs').innerHTML= helpPopup;
          createTabs();
         
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
        
            if(controllerConfig.metaphor=="City original") {
                $.getScript("scripts/HelpController/variantMetaphorCityoriginal.js", function(){   
                     //var content = divContent();
                     createPopupTabs(popupConment());
                });
            };
            
            if(controllerConfig.metaphor=="City bricks") {
                $.getScript("scripts/HelpController/variantMetaphorCitybricks.js", function(){  
                    //var content = divContent();
                    createPopupTabs(popupConment());
                }); 
            };
            
            if(controllerConfig.metaphor=="City floor") {
                $.getScript("scripts/HelpController/variantMetaphorCityfloor.js", function(){  
                    //var content = divContent();
                    createPopupTabs(popupConment());
                    
                }); 
            };

            if(controllerConfig.metaphor=="RD") {
                $.getScript("scripts/HelpController/variantMetaphorRD.js", function(){
                    //var content = divContent();
                    createPopupTabs(popupConment());
                });
            };
        
      });
    }
    
    return {
        initialize: initialize,
		activate: activate
	};    
})();

