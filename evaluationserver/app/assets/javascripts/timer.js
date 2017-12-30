var start_time = Date.now();
jQuery(document).on('page:load ready', function(){
	 start_time = Date.now();
	 jQuery('body').on('submit', 'form', function(){
		  console.log(Date.now()-start_time);
		  jQuery(this).find('.time').val(Date.now()-start_time);
		  console.log(jQuery(this).find('.time'));
	 });
});

function resetTimer() {
	 start_time = Date.now();
}
