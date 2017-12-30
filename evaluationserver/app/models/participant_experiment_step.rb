# coding: utf-8
class ParticipantExperimentStep < ActiveRecord::Base
  belongs_to :experiment_step
  belongs_to :participant

  has_many :participant_experiment_step_scenes, :dependent => :destroy
  has_many :scenes, :through => :participant_experiment_step_scenes

  has_many :participant_experiment_step_answers, :dependent => :destroy
  has_many :answers, :through => :participant_experiment_step_answers
  
  before_save :default_values

  def default_values
    self.id_hash ||= SecureRandom.uuid
  end

  def get_next_step
    raise "Kein Experiment gefunden" unless experiment_step.experiment
    
    if experiment_step.step.parent_step_id
      ## is InnerStep
      step_index = experiment_step.step.parent_step.sub_steps.to_a.index(experiment_step.step)
      if next_inner_step = experiment_step.step.parent_step.sub_steps.to_a[step_index + 1]
        ## es gibt einen nächsten inneren Schritt
        next_experiment_step = ExperimentStep.where(:is_inner_step => true, :step => next_inner_step, :experiment => experiment_step.experiment).first_or_create
        return next_experiment_step.participant_experiment_steps.create(:participant => participant)
      else
        ## es gibt keinen nächsten inneren Schritt
        parent_experiment_step = ExperimentStep.where(:experiment => experiment_step.experiment, :step => experiment_step.step.parent_step).first
        
        experiment_step_index = experiment_step.experiment.experiment_steps.to_a.index(parent_experiment_step)
        if next_experiment_step = parent_experiment_step.experiment.experiment_steps.to_a[experiment_step_index + 1]
          return next_experiment_step.participant_experiment_steps.create(:participant => participant)
        else
          return nil
        end
      end
      

    else
      experiment_step_index = experiment_step.experiment.experiment_steps.to_a.index(experiment_step)
      if next_experiment_step = experiment_step.experiment.experiment_steps.to_a[experiment_step_index + 1]
        return next_experiment_step.participant_experiment_steps.create(:participant => participant)
      else
        return nil
      end
    end
  end


  def get_first_inner_step
    raise "Kein Experiment gefunden" unless experiment_step.experiment
    experiment = experiment_step.experiment
    first_inner_step = experiment_step.step.sub_steps.first
    ### Erzeugen eines neuen ExperimentSteps
    experiment_step = ExperimentStep.where(:is_inner_step => true, :step => first_inner_step, :experiment => experiment).first_or_create
    return experiment_step.participant_experiment_steps.create(:participant => participant)
  end


  def get_parent_participant_experiment_step
    return nil unless experiment_step.experiment
    return nil unless experiment_step.step
    return nil unless experiment_step.step.parent_step
    return nil unless participant
    
    parent_experiment_step = ExperimentStep.where(:experiment => experiment_step.experiment, :step => experiment_step.step.parent_step).first
    
    
    puts parent_experiment_step.participant_experiment_steps.where(:participant => participant).to_yaml
    
    return parent_experiment_step.participant_experiment_steps.where(:participant => participant).last
  end

  

  ### the scene this participant sees
  def get_participant_scene
    return nil if experiment_step.experiment.nil?
    return nil if participant.nil?
    if experiment_step.step and
      parent_participant_experiment_step = get_parent_participant_experiment_step
      return parent_participant_experiment_step.scenes.first
    else
      return scenes.first
    end
  end


  def has_scene?
    return true if scenes.first
    return  !!(experiment_step and experiment_step.step and experiment_step.step.use_scene_from_parent_group and get_participant_scene)
  end


  def answers
    viewed_scene = get_participant_scene
    

      

    
    
    
    
    
    

  end
  
  
  
end
