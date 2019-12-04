var variantMetaphorCityoriginal =(function(){

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
   
    function legend(){
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
            return legend_Cityoriginal; 
    }; 
            
    function relationships(){
            var relationships_Cityoriginal= `
            <div class='relationships_Div jqxTabs_Div helpController'>
                <h2>Inheritance</h2>
                <p>Click on a type to show connections to sub and super types.</p>
                <img src='scripts/HelpController/images/cityoriginal_inheritance.png' >
            </div>`;
            return relationships_Cityoriginal; 
    }; 
    function navigation() {
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
        navigation:navigation,
 		relationships:relationships,
 		legend:legend,
 		tabsUl:tabsUl
 	};
})();