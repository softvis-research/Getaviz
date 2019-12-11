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
                     <div >
                         <img src='scripts/HelpController/images/mouse.png'>
                         <div id='li_Navigation_div' class='helpPopup_Ul_div helpController'>Navigation</div>
                     </div>
                 </li>
             </ul>`;
            
         return helpPopupUl;     
    }; 
    
//     function addItem(){
//         var addNewItem = `
//                 <div style='margin-top: 10px;'>
//                         <input type="button" id='jqxaddItemButton' value="Add new first item item" />
//                 </div>`;
//         return addNewItem; 
//     };
    
    function addLegendTab(){
        var addNewItem = document.createElement("div");
        var text = document.createTextNode('Text');
        addNewItem.appendChild(text);
        
        return addNewItem; 
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
                                height: 750,
                                isModal: true,
                                autoOpen: true,
                                resizable: false
        });
        return loadPopup; 
    };
    
    function popupConment(){
        var helpPopup = "<div id='legendTabs'>" + tabsUl() + variantMetaphor.legend() + variantMetaphor.relationships() + variantNavigation.navigation() + "</div>";
        
        console.log(tabsUl());
        console.log(variantMetaphor.legend());
        console.log(variantMetaphor.relationships());
        console.log(variantNavigation.navigation());
        
        return helpPopup;
    };
            
    function createTabs(){
        
         $("#legendTabs").jqxTabs({ 
                    theme:'metro',
                    width: 550,
                    height: 600,
                    position:'top'
        });
//         $('#jqxaddItemButton').jqxButton({
//             width: '200px',
//             height: 30,
//             theme: 'energyblue'
       // });
        //$('#jqxaddItemButton').click(function () {
            $('#legendTabs').jqxTabs('addFirst', 'Legend', variantMetaphor.legend());
      //  });
        

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
                     $.getScript("scripts/HelpController/variantNavigation.js", function(){
                       createPopupTabs(popupConment());
                       
                    });
                });
            };
            
            if(controllerConfig.metaphor=="City bricks") {
                $.getScript("scripts/HelpController/variantMetaphorCitybricks.js", function(){  
                    $.getScript("scripts/HelpController/variantNavigation.js", function(){
                       createPopupTabs(popupConment());
                    });
                }); 
            };
            
            if(controllerConfig.metaphor=="City floor") {
                $.getScript("scripts/HelpController/variantMetaphorCityfloor.js", function(){  
                   $.getScript("scripts/HelpController/variantNavigation.js", function(){
                       createPopupTabs(popupConment());
                    });
                    
                }); 
            };

            if(controllerConfig.metaphor=="RD") {
                $.getScript("scripts/HelpController/variantMetaphorRD.js", function(){
                    $.getScript("scripts/HelpController/variantNavigation.js", function(){
                       createPopupTabs(popupConment());
                    });
                });
            };
           
      });
    }
    
    return {
        initialize: initialize,
		activate: activate
	};    
})();

