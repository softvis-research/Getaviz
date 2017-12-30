jQuery(document).on('page:load ready', function(){
	 var click_count = 0;
	 jQuery('#scene-iframe').on('load', function(){
		  console.log(jQuery('#scene-iframe').contents().find('a'));
	 }); 
	
	 
	 jQuery('form').on('submit', function(){
		  
	 });
	

});
