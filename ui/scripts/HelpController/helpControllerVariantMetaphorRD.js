var helpControllerVariantMetaphorRD =(function(){

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
   function helpControllerVariantnavigation_x3dom(){
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
            return navigation_x3dom;  
            }; 
            
    function helpControllerVariantnavigation_Aframe(){   
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
               return navigation_Aframe;  
             }; 
             
       function helpControllerVariantlegend_RD(){     
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
            return legend_RD; 
            }; 
       function helpControllerVariantrelationships_RD(){     
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
             
           return relationships_RD; 
      };      
           


   
     return {
        helpControllerVariantrelationships_RD: helpControllerVariantrelationships_RD,
 		helpControllerVariantlegend_RD: helpControllerVariantlegend_RD,
 		helpControllerVarianthelpPopupUl:helpControllerVarianthelpPopupUl,
        helpControllerVariantnavigation_Aframe:helpControllerVariantnavigation_Aframe,
 		helpControllerVariantnavigation_x3dom: helpControllerVariantnavigation_x3dom
 	};
})();
