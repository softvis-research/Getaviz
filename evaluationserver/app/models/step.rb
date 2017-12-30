class Step < ApplicationRecord
  has_many :experiment_steps
  has_many :experiments, :through => :experiment_steps
  
  ### Textual Elements are Texts and Questionaires
  has_many :step_textual_elements
  has_many :textual_elements, :through => :step_textual_elements
  
  
  has_many :step_scenes
  has_many :scenes, :through => :step_scenes
  
  acts_as_list :scope => :parent_step
  belongs_to :parent_step, :class_name => 'GroupedStep'
  has_many :sub_steps, :foreign_key => :parent_step_id, :class_name => 'Step'
  

  def get_random_scene(experiment_step)
    actual_shown_scenes = ParticipantExperimentStep.where(:experiment_step => experiment_step).all.collect{|pes| pes.scenes}.flatten.collect{|scene| scene}
    actual_shown_scenes.select!{|scene| self.scenes.include?(scene)}
    if actual_shown_scenes.uniq.length == 2
      first_scene = actual_shown_scenes.uniq.sort.first
      second_scene = actual_shown_scenes.uniq.sort.last
      
      count_first_scene = actual_shown_scenes.count(first_scene)
      count_second_scene = actual_shown_scenes.count(second_scene)

      random_number = Random.rand(1..actual_shown_scenes.length)
      
      if (1..count_first_scene).include?(random_number)
        return second_scene
      else
        return first_scene
      end
    else
      assigned_scene_number = Random.rand(0..([self.scenes.length-1,0].max))
      puts assigned_scene_number
      puts (self.scenes.length-1)
      puts "***************"
      puts "***************"
      puts "***************"
      return self.scenes[assigned_scene_number]
    end
    
    
  end


  def has_questions?
    if self.is_a?(GroupedStep)
      not(self.sub_steps.find{|sub_step| not(sub_step.textual_elements.where(:type => 'Questionaire').empty?)}.nil?)
    else
      return not(self.textual_elements.where(:type => 'Questionaire').empty?)
    end
  end
  
  
end
