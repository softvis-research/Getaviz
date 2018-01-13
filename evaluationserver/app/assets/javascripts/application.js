// This is a manifest file that'll be compiled into application.js, which will include all the files
// listed below.
//
// Any JavaScript/Coffee file within this directory, lib/assets/javascripts, vendor/assets/javascripts,
// or vendor/assets/javascripts of plugins, if any, can be referenced here using a relative path.
//
// It's not advisable to add code directly here, but if you do, it'll appear at the bottom of the
// compiled file.
//
// Read Sprockets README (https://github.com/sstephenson/sprockets#sprockets-directives) for details
// about supported directives.
//
//= 
//= require jquery
//= require bootstrap-sprockets
//= require jquery_ujs
//  require turbolinks
//= require bootstrap_sortable
//= require sortable
//= require timer
// require click_counter
// require question
//= require tinymce-jquery
//= require bootstrap-multiselect
// require jquery.iframetracker.js
//= require scenetracker
//= require cocoon
//= require multi-select



jQuery(document).ready(function(){
jQuery('.floating-sidebar-toggler').on('click', function(){
	 jQuery(this).parent().find('.floating-sidebar-content').toggle();
	 if (jQuery(this).is(':visible')) {
		  jQuery(this).find('.fa').removeClass('fa-angle-up').addClass('fa-angle-down');
	 } else {
		  jQuery(this).find('.fa').removeClass('fa-angle-down').addClass('fa-angle-up');
	 }
});

	 jQuery('.url-selection').change(showUrl);
	 showUrl();
	 
	 function showUrl() {
		  if (jQuery(this).prop('checked')) {
			  jQuery('.url').show();
			  jQuery('.no-url').hide();
		 } else {
			  jQuery('.url').hide();
			  jQuery('.no-url').show();
		 }
	 }
	 $('select.multiselect').multiSelect({ keepOrder: true });

	 initializeSlider()


	 
	 
});



function initializeSlider() {
	 function updateRange(element){
		  console.log('AAAAAAAa');
		  console.log(jQuery(element))
		  jQuery(element).parent().find('.range_value').html(jQuery(element).val());
	 }

	 jQuery('input[type="range"]').on('input', function(){
		  updateRange(jQuery(this));
	 });
	 jQuery('input[type="range"]').each(function(){
		  console.log("AAA");
		  updateRange(this);
	 });

}


function fullscreen(){
	 var element = document.getElementsByTagName("body")[0];
	 if (element.requestFullScreen) {
		  element.requestFullScreen();
	 } else if (element.mozRequestFullScreen) {
		  element.mozRequestFullScreen();
	 } else if (element.webkitRequestFullScreen) {
		  element.webkitRequestFullScreen();
	 }
}

jQuery(document).ajaxComplete(function(){
	 initializeSlider();
});
