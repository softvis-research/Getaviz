var metaphor =(function(){

    function legend(){       
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
             
    function relationships(){         
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
   
    return {
 		relationships:relationships,
 		legend: legend
 	};
})();
