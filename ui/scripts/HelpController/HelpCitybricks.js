var metaphor =(function(){
    
    function legend(){            
            var legend_Citybricks = `
            <div class='legend_Div jqxTabs_Div helpController'>
                <p>
                The city metaphor is a 3 dimensional real-world metaphor, aimed at creating a better understanding of the visualized system through a natural environment. The city consists of districts and buildings. The buildings are arranged so that the district area is as small and square as possible.
                </p>
                <img src='scripts/HelpController/images/citybricks.PNG'>
                <div class='legend_Describe helpController'>
                    <h2>District</h2>
                    <img src='scripts/HelpController/images/cityoriginal_package.png'>
                    <p>Districts represent <span class='legend_Represent helpController'>packages</span>. The districts and building in the district represent the components of the package, i.e., sub packages and types. The area of a district depends on the size of districts and buildings inside.</p>
                </div>
                <div class='legend_Describe helpController'>
                    <h2>Building base</h2>
                    <img src='scripts/HelpController/images/bricks_buildingbase.png'>
                    <p>Buildings consists of a building base and bricks. The building base represents <span class='legend_Represent helpController'>types</span>, bricks the corresponding methods and fields.</p>
                </div>
                <div class='bricks_Describe helpController'>
                    <h2>Brick</h2>
                    <img src='scripts/HelpController/images/bricks_red.png' >
                    <p>Red bricks represent <span class='legend_Represent helpController'>fields with primitive types</span>.</p>
                </div>
                <div class='bricks_Describe helpController'>
                    <img src='scripts/HelpController/images/bricks_lightgreen.png'>
                    <p>Light green bricks represent <span class='legend_Represent helpController'>getter methods</span>.</p>
                </div>
                <div class='bricks_Describe helpController'>
                    <img src='scripts/HelpController/images/bricks_darkgreen.png'>
                    <p>Dark green bricks represent <span class='legend_Represent helpController'>setter methods</span>.</p>
                </div>
                <div class='bricks_Describe helpController'>
                    <img src='scripts/HelpController/images/bricks_orange.png'>
                    <p>Orange bricks represent <span class='legend_Represent helpController'>abstract methods</span>.</p>
                </div>
                <div class='bricks_Describe helpController'>
                    <img src='scripts/HelpController/images/bricks_yellow.png'>
                    <p>Yellow bricks represent <span class='legend_Represent helpController'>static methods</span>.</p>
                </div>
                <div class='bricks_Describe helpController'>
                    <img src='scripts/HelpController/images/bricks_purple.png'>
                    <p>Purple bricks represent all other <span class='legend_Represent helpController'>methods</span>.</p>
                </div>
                <div class='bricks_Describe helpController'>
                    <img src='scripts/HelpController/images/bricks_pink.png'>
                    <p>Pink bricks represent <span class='legend_Represent helpController'>constructors</span>.</p>
                </div>
                <div class='bricks_Describe helpController'>
                    <img src='scripts/HelpController/images/bricks_blue.png'>
                    <p>Blue bricks represent <span class='legend_Represent helpController'>fields with complex types</span>.</p>
                </div>
            </div>`;
            
            return legend_Citybricks; 
    }; 
            
    function relationships(){         
            var relationships_Citybricks= `
            <div class='relationships_Div jqxTabs_Div helpController'>
                <h2>Field accesses</h2>
                <p>Click on a field to show connections to accessing methods.</p>
                <p>Click on a method to show connections to accessed field.</p>
                <img src='scripts/HelpController/images/citybricks_void.png' >
                <h2>Method calls</h2>
                <p>Click on a method to show in- and outgoing method calls.</p>
                <img src='scripts/HelpController/images/citybricks_credits.png'>
            </div>`;
            return relationships_Citybricks; 
    }; 
   
    return {
 		relationships:relationships,
 		legend: legend
 	};
})();
