var metaphor =(function(){

    function legend(){     
            var legend_RD = `
            <div class='legend_Div jqxTabs_Div helpController'>
                <p>The Recursive Disk (RD) metaphor is designed to visualize the structure of imperative programming languages, with an emphasis on object-oriented languages. As the name indicates, an RD visualization consists of nested disks, where each disk represents a package or a class. All disks are ordered by size and then placed spiral-shaped clockwise around the largest disk. Although at first glance it seems chaotic, the emerging visual patterns and empty spaces give each disk a unique appearance and help the user to recognize specific disks.</p>
                <img src='scripts/HelpController/images/RD.PNG'>
                <div class='legend_Describe helpController'>
                    <h2>Gray Disk</h2>
                    <img src='scripts/HelpController/images/RD_package.png'>
                    <p>Gray disks represent <span class='legend_Represent helpController'>translation units</span>. The nested disks represent its components, i.e., types. The size of a disk depends on the size of the nested disks.</p>
                </div>
                <div class='legend_Describe helpController'>
                    <h2>Purple Disk</h2>
                    <img src='scripts/HelpController/images/RD_type.png'>
                    <p>Purple disks represent <span class='legend_Represent helpController'>types</span>. The size of a disk depends on the size of the nested disks and segments.</p>
                </div>
                <div class='legend_Describe helpController'>
                    <h2>Blue Segment</h2>
                    <img src='scripts/HelpController/images/RD_method.png'>
                    <p>Blue segments represent <span class='legend_Represent helpController'>functions</span>. The area of a blue segment is based on the functions lines of code.</p>
                </div>
                <div class='legend_Describe helpController'>
                    <h2>Yellow Segment</h2>
                    <img src='scripts/HelpController/images/RD_field.png'>
                    <p>Yellow segements represent <span class='legend_Represent helpController'>variables</span>. The area of a yellow segment is fixed and does not represent a metric.</p>
                </div>
            </div>`;
            return legend_RD; 
        }; 
            
       function relationships(){     
            var relationships_RD= `
           <div class='relationships_Div jqxTabs_Div helpController'>
                <h2>Variable accesses</h2>
                <p>Click on a variable to show connections to accessing functions.</p>
                <p>Click on a function to show connections to accessed variables.</p>
                <img src='scripts/HelpController/images/RD_void.png'>
                <h2>Function calls</h2>
                <p>Click on a function to show in- and outgoing function calls.</p>
                <img src='scripts/HelpController/images/RD_voidrun.png'>
           </div>`;
             
           return relationships_RD; 
       };      

     return {
        relationships: relationships,
 		legend: legend
 	};
})();
