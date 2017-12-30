var last_uuid = null;
var actual_uuid = null;
var user_interaction_offset = {
	 'clicks' : 0,
	 'mousepressed' : 0,
	 'mousewheel' : 0,
	 'sceneresets' : 0
}


jQuery(window).on("message", function(e) {
	 
    var data = e.originalEvent.data;
	 console.log(data);
	 var new_uuid = data['uuid'];
	 if (new_uuid != actual_uuid) {
		  // der Nutzer hat neu geladen
		  // wir nutzen die letzten Daten die wir haben als Offset
		  user_interaction_offset = {
				'clicks' : parseFloat(jQuery('form').find('.iframe_click_counter').val()),
				'mousepressed' : parseFloat(jQuery('form').find('.iframe_mousedown_time').val()),
				'mousewheel' : parseFloat(jQuery('form').find('.iframe_mousewheel_scroll').val()),
				'sceneresets' : parseFloat(jQuery('form').find('.iframe_sceneresets').val())
		  };
		  new_uuid == actual_uuid;
	 }
	 
	 
	 // do something with the informations
	 jQuery('form').find('.iframe_click_counter').val(parseFloat(data['clicks']) + user_interaction_offset['clicks']);
	 jQuery('form').find('.iframe_mousedown_time').val(parseFloat(data['mousepressed']) + user_interaction_offset['mousepressed']);
	 jQuery('form').find('.iframe_mousewheel_scroll').val(parseFloat(data['mousewheel']) + user_interaction_offset['mousewheel']);
	 jQuery('form').find('.iframe_sceneresets').val(parseFloat(data['sceneresets']) + user_interaction_offset['sceneresets']);


	 
});
