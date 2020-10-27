var abapNavigationController = (function() {
   
  function initialize(){
  }
	
	function activate(){
    actionController.actions.mouse.scroll.subscribe(zoom);
  }

  function zoom(event){
    const delta = Math.sign(event.wheelDelta) * 0.1;
      //getting the mouse wheel change (120 or -120 and normalizing it to 1 or -1)
      var mycam=document.getElementById('cam').getAttribute('camera');
      var finalZoom=document.getElementById('cam').getAttribute('camera').zoom+delta;
      //limiting the zoom so it doesnt zoom too much in or out
      if(finalZoom<1)
        finalZoom=1;            
      if(finalZoom>10)
        finalZoom=10;   
  
      mycam.zoom=finalZoom;
      //setting the camera element
      document.getElementById('cam').setAttribute('camera',mycam);
  }


    return {
        initialize: initialize,
		    activate: activate
    }; 

})();