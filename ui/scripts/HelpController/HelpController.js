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
            
    
            
//              var helpPopupUl = document.createElement("ul");
//              var vonSeite;
//              vonSeite = window.open("scripts/HelpController/jqxTabs_Html/helpPopup_Tabs_Ul.html");
//              helpPopupUl.value = vonSeite.document.body.innerHTML;
//              vonSeite.close();
            
            
//              var helpPopupUlDiv;
//              var vonSeite;
//              vonSeite = window.open("scripts/HelpController/jqxTabs_Html/helpPopup_Tabs_Ul.html");
//              helpPopupUlDiv.value = vonSeite.document.body.innerHTML;
//              vonSeite.close();
//              console.log(helpPopupUlDiv);
            
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

            var helpPopup_Navigation;
            var helpPopup_Legend;
            var helpPopup;
            
            var navigation_x3dom = `
            <div class='jqxTabs_Div helpController'>
                <div class='navigation_Describe helpController'>
                    <h2>Rotate</h2>
                    <img src='scripts/Legend/images/left.png'>
                    <p>Hold down the left mouse button and move the mouse to rotate the visualization.</p>
                </div>
                <div class='navigation_Describe helpController'>
                    <h2>Center</h2>
                    <img src='scripts/Legend/images/double.png'>
                    <p>Double-click on any location to center it.</p>
                </div>
                <div class='navigation_Describe helpController'>
                    <h2>Move</h2>
                    <img src='scripts/Legend/images/middle.png' >
                    <p>Hold down the middle mouse button and move the mouse to move the visualization.</p>
                </div>
                <div class='navigation_Describe helpController'>
                    <h2>Zoom</h2>
                    <img src='scripts/Legend/images/scrolling.png'>
                    <p>Use the scroll wheel to zoom in and out.</p>
                </div>
            </div>`;
            
            var navigation_Aframe = `
            <div class='jqxTabs_Div helpController'>
                <div class='navigation_Describe helpController'>
                    <h2>Rotate</h2>
                    <img src='scripts/Legend/images/left.png'>
                    <p>Hold down the left mouse button and move the mouse to rotate the visualization.</p>
                </div>
                <div class='navigation_Describe helpController'>
                    <h2>Move</h2>
                    <img src='scripts/Legend/images/middle.png' >
                    <p>Hold down the middle mouse button and move the mouse to move the visualization.</p>
                </div>
                <div class='navigation_Describe helpController'>
                    <h2>Zoom</h2>
                    <img src='scripts/Legend/images/scrolling.png'>
                    <p>Use the scroll wheel to zoom in and out.</p>
                </div>
            </div>`;
            
            if (visMode.includes( "x3dom")) {
                helpPopup_Navigation = navigation_x3dom;
                } else{
                helpPopup_Navigation = navigation_Aframe;
            };
            
            var legend_Cityoriginal = `
            <div class='legend_Div jqxTabs_Div helpController'>
                <p>The city metaphor is a 3 dimensional real-world metaphor, aimed at creating a better understanding of the visualized system through a natural environment. The city consists of districts and buildings. The buildings are arranged so that the district area is as small and square as possible.</p>
                <img src='scripts/HelpController/images/city.PNG'>
                <div class='legend_Describe helpController'>
                    <h2>District</h2>
                    <img src='scripts/HelpController/images/cityoriginal_package.png' >
                    <p>Districts represent <span class='legend_Represent helpController'>packages</span>. The districts and building in the district represent the components of the package, i.e., sub packages and types. The area of a district depends on the size of districts and buildings inside.</p>
                </div>
                <div class='legend_Describe helpController'>
                    <h2>Building</h2>
                    <img src='scripts/HelpController/images/cityoriginal_type.png' >
                    <p>Buildings represent <span class='legend_Represent helpController'>types</span>. The height of the buildings corresponds to the number of methods of the corresponding type. The floor areas of the buildings are square. The width corresponds to the number of fields.</p>
                </div>
            </div>`;
            
            var relationships_Cityoriginal= `
            <div class='relationships_Div jqxTabs_Div helpController'>
                <h2>Inheritance</h2>
                <p>Click on a type to show connections to sub and super types.</p>
                <img src='scripts/HelpController/images/cityoriginal_inheritance.png' >
            </div>`;
            
            if(controllerConfig.metaphor=="City original") {
                helpPopup_Legend = legend_Cityoriginal;
                helpPopup = "<div id='legendTabs'>" + helpPopupUl + helpPopup_Legend + relationships_Cityoriginal + helpPopup_Navigation + "</div>";
            };
            
            var legend_Citybricks = `
            <div class='legend_Div jqxTabs_Div helpController'>
                <p>
                The city metaphor is a 3 dimensional real-world metaphor, aimed at creating a better understanding of the visualized system through a natural environment. The city consists of districts and buildings. The buildings are arranged so that the district area is as small and square as possible.</p>
                <img src='scripts/HelpController/images/citybricks.PNG'>
                <div class='legend_Describe helpController'>
                    <h2>District</h2>
                    <img src='scripts/HelpController/images/cityoriginal_package.png' >
                    <p>Districts represent <span class='legend_Represent helpController'>packages</span>. The districts and building in the district represent the components of the package, i.e., sub packages and types. The area of a district depends on the size of districts and buildings inside.</p>
                </div>
                <div class='legend_Describe helpController'>
                    <h2>Building base</h2>
                    <img src='scripts/HelpController/images/bricks_Buildingbase.png' >
                    <p>Buildings consists of a building base and bricks. The building base represents <span class='legend_Represent helpController'>types</span>, bricks the corresponding methods and fields.</p>
                </div>
                <div class='legend_Describe helpController'>
                    <h2>Brick</h2>
                    <img src='scripts/HelpController/images/bricks_Purple.png' >
                    <p>The city metaphor is a 3 dimensional real-world metaphor, aimed at creating a better understanding of the visualized system through a natural environment.</p>
                </div>
                <div class='legend_Describe helpController'>
                    <img src='scripts/HelpController/images/bricks_Voilet.png' >
                    <p>The city metaphor is a 3 dimensional real-world metaphor, aimed at creating a better understanding of the visualized system through a natural environment.</p>
                </div>
                <div class='legend_Describe helpController'>
                    <img src='scripts/HelpController/images/bricks_Mint.png' >
                    <p>The city metaphor is a 3 dimensional real-world metaphor, aimed at creating a better understanding of the visualized system through a natural environment.</p>
                </div>
                <div class='legend_Describe helpController'>
                    <img src='scripts/HelpController/images/bricks_Sunyellow.png' >
                    <p>The city metaphor is a 3 dimensional real-world metaphor, aimed at creating a better understanding of the visualized system through a natural environment.</p>
                </div>
                <div class='legend_Describe helpController'>
                    <img src='scripts/HelpController/images/bricks_Pink.png' >
                    <p>The city metaphor is a 3 dimensional real-world metaphor, aimed at creating a better understanding of the visualized system through a natural environment.</p>
                </div>
                <div class='legend_Describe helpController'>
                    <img src='scripts/HelpController/images/bricks_Lightblue.png' >
                    <p>The city metaphor is a 3 dimensional real-world metaphor, aimed at creating a better understanding of the visualized system through a natural environment.</p>
                </div>
                <div class='legend_Describe helpController'>
                    <img src='scripts/HelpController/images/bricks_Yellow.png' >
                    <p>The city metaphor is a 3 dimensional real-world metaphor, aimed at creating a better understanding of the visualized system through a natural environment.</p>
                </div>
                <div class='legend_Describe helpController'>
                    <img src='scripts/HelpController/images/bricks_Lime.png' >
                    <p>The city metaphor is a 3 dimensional real-world metaphor, aimed at creating a better understanding of the visualized system through a natural environment.</p>
                </div>
            </div>`;
             
            var relationships_Citybricks= `
            <div class='relationships_Citybricks_Div relationships_Div helpController'>
                <h2>Field accesses</h2>
                <p>Click on a field to show connections to accessing methods.</p>
                <p>Click on a method to show connections to accessed field.</p>
                <img src='scripts/HelpController/images/citybricks_void.png' >
                <h2>Method calls</h2>
                <p>Click on a method to show in- and outgoing method calls.</p>
                <img src='scripts/HelpController/images/citybricks_credits.png'>
            </div>`;
              
            if(controllerConfig.metaphor=="City bricks") {
                helpPopup_Legend = legend_Citybricks;
                helpPopup = "<div id='legendTabs'>" + helpPopupUl + helpPopup_Legend + relationships_Citybricks + helpPopup_Navigation + "</div>";
             }; 
             
            var legend_Cityfloor = `
             <div class='legend_Div jqxTabs_Div helpController'>
                <p>The city metaphor is a 3 dimensional real-world metaphor, aimed at creating a better understanding of the visualized system through a natural environment. The city consists of districts and buildings. The buildings are arranged so that the district area is as small and square as possible.</p>
                <img src='scripts/HelpController/images/cityfloor.PNG'>
                <div class='legend_Describe helpController'>
                    <h2>District</h2>
                    <img src='scripts/HelpController/images/cityoriginal_package.png' >
                    <p>Districts represent <span class='legend_Represent helpController'>packages</span>. The districts and building in the district represent the components of thepackage, i.e., sub packages and types. The area of a district depends on the size of districts and buildings inside.</p>
                </div>
                <div class='legend_Describe helpController'>
                    <h2>Building</h2>
                    <img src='scripts/HelpController/images/cityfloor_type.png' >
                    <p>Buildings consists of a building base, floors and bricks. The building base represents <span class='legend_Represent helpController'>types</span>, floors and chimneys the corresponding methods and fields.</p>
                </div>
                <div class='legend_Describe helpController'>
                    <h2>Floor</h2>
                    <img src='scripts/HelpController/images/cityfloor_method.png' >
                    <p>Floors represent <span class='legend_Represent helpController'>methods</span>. The size of floor is fixed and not based on a metric.</p>
                </div>
                <div class='legend_Describe helpController'>
                    <h2>Chimney</h2>
                    <img src='scripts/HelpController/images/cityfloor_field.png' >
                    <p>Chimneys represend <span class='legend_Represent helpController'>fields</span>. The size of a chimney is fixed and not based on a metric.</p>
                </div>
             </div>`;
             
            var relationships_Cityfloor= `
            <div class='relationships_Div jqxTabs_Div helpController'>
                <h2>Field accesses</h2>
                <p>Click on a field to show connections to accessing methods.</p>
                <p>Click on a method to show connections to accessed field.</p>
                <img src='scripts/HelpController/images/cityfloor_void.png' >
                <h2>Method calls</h2>
                <p>Click on a method to show in- and outgoing method calls.</p>
                <img src='scripts/HelpController/images/cityfloor_credits.png' >
            </div>`;
            
            if(controllerConfig.metaphor=="City floor") {
                helpPopup_Legend = legend_Cityfloor;
                helpPopup = "<div id='legendTabs'>" + helpPopupUl + helpPopup_Legend + relationships_Cityfloor + helpPopup_Navigation + "</div>";
             };
             
            var legend_RD = `
            <div class='legend_Div jqxTabs_Div helpController'>
                <p>The Recursive Disk (RD) metaphor is designed to visualize the structure of imperative programming languages, with an emphasis on object-oriented languages. As the name indicates, an RD visualization consists of nested disks, where each disk represents a package or a class. All disks are ordered by size and then placed spiral-shaped clockwise around the largest disk. Although at first glance it seems chaotic, the emerging visual patterns and empty spaces give each disk a unique appearance and help the user to recognize specific disks.</p>
                <img src='scripts/HelpController/images/RD.PNG'>
                <div class='legend_Describe helpController'>
                    <h2>Gray Disk</h2>
                    <img src='scripts/HelpController/images/RD_package.png' >
                    <p>Gray disks represent <span class='legend_Represent helpController'>packages</span>. The nested disks represent the components of the package, i.e., sub packages and types. The size of a disk depends on the size of the nested disks.</p>
                </div>
                <div class='legend_Describe helpController'>
                    <h2>Purple Disk</h2>
                    <img src='scripts/HelpController/images/RD_type.png' >
                    <p>Purple disks represent <span class='legend_Represent helpController'>types</span>. The nested disks represent inner types. The size of a disk depends on the size of the nested disks and segments.</p>
                </div>
                <div class='legend_Describe helpController'>
                    <h2>Blue Segment</h2>
                    <img src='scripts/HelpController/images/RD_method.png'><p>Blue segments represent <span class='legend_Represent helpController'>methods</span>. The area of a blue segment is based on the methods lines of code.</p>
                </div>
                <div class='legend_Describe helpController'>
                    <h2>Yellow Segment</h2>
                    <img src='scripts/HelpController/images/RD_field.png' >
                    <p>Yellow segements represent <span class='legend_Represent helpController'>fields</span>. The area of a yellow segment is fixed and does not represent a metric.</p>
                </div>
            </div>`;
            
            var relationships_RD= `
           <div class='relationships_Div jqxTabs_Div helpController'>
                <h2>Inheritance</h2>
                <p>Click on a type to show connections to sub and super types.</p>
                <img src='scripts/HelpController/images/RD_abstract.png' >
                <h2>Field accesses</h2>
                <p>Click on a field to show connections to accessing methods.</p>
                <p>Click on a method to show connections to accessed field.</p>
                <img src='scripts/HelpController/images/RD_void.png' >
                <h2>Method calls</h2>
                <p>Click on a method to show in- and outgoing method calls.</p>
                <img src='scripts/HelpController/images/RD_voidrun.png' >
           </div>`;
             
           
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
	}
	
    return {
        initialize: initialize,
		activate: activate
	};    
})();

