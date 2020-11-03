var bannerController = (function() {

  function initialize(){
  }

  function activate(rootDiv){
    var banner = document.createElement("img");
    banner.id = "banner";
    banner.src = "scripts/ABAP/images/banner3.png";

    rootDiv.appendChild(banner);

    $("#banner").jqxButton({ 
      theme: "metro",
      width: "99.25%", 
      height: "80px",
  });
  }

  return {
    initialize: initialize,
	  activate: activate
  }; 

})();