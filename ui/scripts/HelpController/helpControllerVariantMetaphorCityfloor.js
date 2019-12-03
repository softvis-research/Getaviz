var helpControllerVariantMetaphorCityfloor =(function(){

    function helpControllerVarianthelpPopupUl(){
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
    
 
            
    function helpControllerVariantlegend_Cityfloor(){       
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
             return legend_Cityfloor; 
    }; 
             
    function helpControllerVariantrelationships_Cityfloor(){         
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
            return relationships_Cityfloor; 
    }; 
   function helpControllerNavigation() {
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
             var helpPopup_Navigation;
             if (visMode.includes( "x3dom")) {
                 helpPopup_Navigation = navigation_x3dom;
             } else{
                 helpPopup_Navigation = navigation_Aframe;
             };
             return helpPopup_Navigation; 
    }

    return {
        helpControllerNavigation:helpControllerNavigation,
 		helpControllerVariantrelationships_Cityfloor:helpControllerVariantrelationships_Cityfloor,
 		helpControllerVariantlegend_Cityfloor: helpControllerVariantlegend_Cityfloor,
 		helpControllerVarianthelpPopupUl:helpControllerVarianthelpPopupUl
 	};
})();
