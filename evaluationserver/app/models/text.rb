class Text < TextualElement


  def rendered_html(participant_experiment_step)
    if participant_experiment_step
      participant_scene = participant_experiment_step.get_participant_scene
      if participant_scene
        return html.gsub(/<p>\$MAPPING<\/p>/, participant_scene.mapping.to_s).gsub(/<p>\$HINWEIS<\/p>/, participant_scene.remarks.to_s)
      else
        return html
      end
    else
      return html
    end
    
    
  end
  
end
