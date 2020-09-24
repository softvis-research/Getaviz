var antipattern =(function(){

    function antipattern() {
        var  helpPopup_Antipattern;
        if (visMode.includes( "x3dom")) {
            var antipattern_x3dom = `
                     <div class='jqxTabs_Div helpController'>
                        <div class='antipattern_Describe helpController'>
                            <h2>God Class</h2>
                            <p>The class centralizes the logic of the system and only assigns minor tasks to other very 
                            simple classes. This goes against the principle of object oriented design that every class 
                            should only have one responsibility and can hamper the reusability and understandability 
                            of the system.</p>
                        </div>
                        <div class='antipattern_Describe helpController'>
                            <h2>Feature Envy</h2>
                            <p>This method access more data from other classes than from their own, it might be
                             misplaced. Usually data and operations should be together as close as possible to increase 
                             cohesion and reduce ripple effects. </p>
                        </div>
                        <div class='antipattern_Describe helpController'>
                            <h2>Data Class</h2>
                            <p>A class that has no complex functionality and is a mere data holder for other classes. 
                            These classes break good object-oriented design since they let other classes change their 
                            own data which can reduce maintainability and understandability.</p>
                        </div>
                        <div class='antipattern_Describe helpController'>
                            <h2>Brain Method</h2>
                            <p>These Methods centralize the classes functionality, they are often too long
                             therefore hard to understand, debug and reuse.</p>
                        </div>
                         <div class='antipattern_Describe helpController'>
                            <h2>Brain Class</h2>
                            <p>Similar to the God Class, Brain Classes centralize the intelligence and are very complex.
                             The main difference is that they don't access as much foreign data and are therefore not
                              God Classes. A Brain Class contains at least one Brain Method.</p>
                        </div>
                    </div>`;
            helpPopup_Antipattern = antipattern_x3dom;

        } else {
            var antipattern_Aframe = `
                     <div class='jqxTabs_Div helpController'>
                        <div class='antipattern_Describe helpController'>
                            <h2>God Class</h2>
                            <p>The class centralizes the logic of the system and only assigns minor tasks to other very 
                            simple classes. This goes against the principle of object oriented design that every class 
                            should only have one responsibility and can hamper the reusability and understandability 
                            of the system.</p>
                        </div>
                        <div class='antipattern_Describe helpController'>
                            <h2>Feature Envy</h2>
                            <p>This method access more data from other classes than from their own, it might be
                             misplaced. Usually data and operations should be together as close as possible to increase 
                             cohesion and reduce ripple effects. </p>
                        </div>
                        <div class='antipattern_Describe helpController'>
                            <h2>Data Class</h2>
                            <p>A class that has no complex functionality and is a mere data holder for other classes. 
                            These classes break good object-oriented design since they let other classes change their 
                            own data which can reduce maintainability and understandability.</p>
                        </div>
                        <div class='antipattern_Describe helpController'>
                            <h2>Brain Method</h2>
                            <p>These Methods centralize the classes functionality, they are often too long
                             therefore hard to understand, debug and reuse.</p>
                        </div>
                         <div class='antipattern_Describe helpController'>
                            <h2>Brain Class</h2>
                            <p>Similar to the God Class, Brain Classes centralize the intelligence and are very complex.
                             The main difference is that they don't access as much foreign data and are therefore not
                              God Classes. A Brain Class contains at least one Brain Method.</p>
                        </div>
                    </div>`;
            helpPopup_Antipattern = antipattern_Aframe;
        };
        return  helpPopup_Antipattern;
    }

    return {
        antipattern:antipattern

    };
})();
