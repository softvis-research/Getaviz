var navigation =(function(){

    function navigation() {
        var  helpPopup_Navigation;
            if (visMode.includes( "x3dom")) {
                var navigation_x3dom = `
                    <div class='jqxTabs_Div helpController'>
                        <div class='navigation_Describe helpController'>
                            <h2>Rotate</h2>
                            <img src='scripts/HelpController/images/left.png'>
                            <p>Hold down the left mouse button and move the mouse to rotate the visualization.</p>
                        </div>
                        <div class='navigation_Describe helpController'>
                            <h2>Center</h2>
                            <img src='scripts/HelpController/images/double.png'>
                            <p>Double-click on any location to center it.</p>
                        </div>
                        <div class='navigation_Describe helpController'>
                            <h2>Move</h2>
                            <img src='scripts/HelpController/images/middle.png' >
                            <p>Hold down the middle mouse button and move the mouse to move the visualization.</p>
                        </div>
                        <div class='navigation_Describe helpController'>
                            <h2>Zoom</h2>
                            <img src='scripts/HelpController/images/scrolling.png'>
                            <p>Use the scroll wheel to zoom in and out.</p>
                        </div>
                    </div>`;
                helpPopup_Navigation = navigation_x3dom;
                
            } else  {
                var navigation_Aframe = `
                    <div class='jqxTabs_Div helpController'>
                        <div class='navigation_Describe helpController'>
                            <h2>Rotate</h2>
                            <img src='scripts/HelpController/images/left.png'>
                            <p>Hold down the left mouse button and move the mouse to rotate the visualization.</p>
                        </div>
                        <div class='navigation_Describe helpController'>
                            <h2>Move</h2>
                            <img src='scripts/HelpController/images/middle.png' >
                            <p>Hold down the middle mouse button and move the mouse to move the visualization.</p>
                        </div>
                        <div class='navigation_Describe helpController'>
                            <h2>Zoom</h2>
                            <img src='scripts/HelpController/images/scrolling.png'>
                            <p>Use the scroll wheel to zoom in and out.</p>
                        </div>
                    </div>`;
                 helpPopup_Navigation = navigation_Aframe;
            };
            return  helpPopup_Navigation; 
        }
        
    return {
        navigation:navigation

 	};
})();
