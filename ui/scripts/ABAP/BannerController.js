var bannerController = (function() {

  function initialize(){
  }

  function activate(rootDiv){
    var banner = document.createElement("img");
    banner.id = "banner";
    banner.src = "scripts/ABAP/images/banner.png";

    rootDiv.appendChild(banner);

    $("#banner").jqxButton({ 
      theme: "metro",
      width: "99%", 
      height: "70px",
  });
  }

  return {
    initialize: initialize,
	  activate: activate
  }; 

})();