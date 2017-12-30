
function initializeQuestionSelect() {
	 initializeControlGroups();
	 jQuery(".question-type").change(function() {
		  initializeControlGroups();
	 });
	 
	 jQuery('.choice-text').first().find('.delete-option').first().hide();
	 
	 jQuery('.new-option').click(function(){
		  
		  jQuery('.choice-text').first().parent().clone().insertAfter(jQuery('.choice-text').last().parent());
		  jQuery('.choice-text').last().find('.delete-option').first().show();
		  jQuery('.choice-text').last().find("input.text_field").val("");
		  
	 });
	 jQuery(document).on('click', '.delete-option', function(){
		  jQuery(this).parents('.control-group').remove();
		  
	 });

}

function initializeControlGroups() {
	 var type = jQuery(".question-type").val();
	 var text = jQuery(".question-type").find('option[value=' + type + ']').text();
	 if (text.search(/Slider/) !== -1) {
		  jQuery('.choice-options').hide();
		  jQuery('.slider-options').show();
	 } else {
		  if (text.search(/[Ch]oice/) !== -1) {
				jQuery('.choice-options').show();
				jQuery('.slider-options').hide();
		  } else {
				jQuery('.choice-options').hide();
				jQuery('.slider-options').hide();
		  }
	 }
}


jQuery(document).ready(initializeQuestionSelect);
jQuery(document).on('page:load', initializeQuestionSelect);
