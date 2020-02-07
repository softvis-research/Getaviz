# coding: utf-8
class Experiment < ApplicationRecord
  has_many :participants
  has_many :experiment_steps, ->{ where(:is_inner_step => [false,nil]).order('experiment_steps.position, experiment_steps.id') }
  has_many :steps, :through => :experiment_steps
  
  before_save :default_values

  def default_values
    self.id_hash ||= SecureRandom.uuid
  end

  
  def results(complete = false)
    
    number_of_steps = steps.length
    questions = steps.collect{|step| step.textual_elements.where(:type => 'Questionaire').collect{|questionaire| questionaire.questions }}.flatten
    scenes = steps.collect{|step|  step.step_scenes }.flatten
    participants = experiment_steps.collect{|step| step.participants }.flatten.uniq
    if complete
      results = experiment_steps.collect(&:step_results)
    else
      results = nil
    end
    return {
      :number_of_steps => number_of_steps,
      :number_of_questions => questions.length,
      :number_of_scenes => scenes.length,
      :number_of_participants => participants.length,
      :step_results => results
    }
    
  end


  def all_answers
    all_participant_experiment_steps_answers = ParticipantExperimentStepAnswer.joins(:participant_experiment_step => [:experiment_step => [:experiment]]).where(Experiment.arel_table[:id].eq(self.id))

    return all_participant_experiment_steps_answers.collect{|pesa|
      question =  pesa.question.question_text
      correct_answer = pesa.question.correct_answer_cleartext
      answer = pesa.given_answer_cleartext
      participant = pesa.participant.id
      scene = pesa.related_scene.name if pesa.related_scene
      step = nil
      if pesa.participant_experiment_step.get_parent_participant_experiment_step
        step = pesa.participant_experiment_step.get_parent_participant_experiment_step.experiment_step.step.title
      else
        step = pesa.participant_experiment_step.experiment_step.step.title
      end
      {
        :question => question,
        :answer => answer,
        :correct_answer => correct_answer,
        :participant => participant,
        :scene => scene,
        :step => step,
        :mistakes => pesa.mistakes,
        :calculated_mistakes => pesa.number_of_mistakes_in_final_answer,
        :answered_correctly => pesa.answered_correctly,
        :time_needed_in_ms => pesa.time_needed_in_ms,
        :helps => pesa.helps
      }
      
      
    }
    

  end


  def delete_results
    participants.destroy_all
  end

  

end
