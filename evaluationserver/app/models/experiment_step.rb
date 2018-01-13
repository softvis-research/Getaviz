class ExperimentStep < ApplicationRecord
  
  belongs_to :experiment
  belongs_to :step
  acts_as_list :scope => :experiment
  
  
  has_many :participant_experiment_steps
  has_many :participants, :through => :participant_experiment_steps
  before_save :default_values
  
  
  def default_values
    self.id_hash ||= SecureRandom.uuid
  end


  def result
    all_participant_experiment_steps = ParticipantExperimentStep.joins(:experiment_step => [:experiment]).where(Experiment.arel_table[:id].eq(self.experiment_id))



    








    all_participant_experiment_steps_answers = ParticipantExperimentStepAnswer.joins(:participant_experiment_step => [:experiment_step => [:experiment]]).where(Experiment.arel_table[:id].eq(self.experiment_id))
    all_participant_experiment_steps_scenes = ParticipantExperimentStepScene.joins(:participant_experiment_step => [:experiment_step => [:experiment]]).where(Experiment.arel_table[:id].eq(self.experiment_id))
    
    experiment_step_result = {:experiment_step => id}
    step_participant_experiment_steps_answers = all_participant_experiment_steps_answers.where(ExperimentStep.arel_table[:id].eq(id))
    step_participant_experiment_steps_scenes = all_participant_experiment_steps_scenes.where(ExperimentStep.arel_table[:id].eq(id))
    
    if step_participant_experiment_steps_answers.size > 0
      if step_participant_experiment_steps_scenes.size > 0
        puts "ES with questions with scene #{id}"
        experiment_step_result[:step_type] = :scene_with_questions
        experiment_step_result[:result] = {}
        number_of_clicks = []
        time_mouse_down = []
        experiment_step_result[:result][:scenes] = Scene.where(:id => step_participant_experiment_steps_scenes.pluck(:scene_id)).collect{|scene|
          scene_informations = step_participant_experiment_steps_scenes.where(:scene_id => scene)
          min_number_mouseclicks = scene_informations.pluck(:number_of_clicks).collect(&:to_i).min
          max_number_mouseclicks = scene_informations.pluck(:number_of_clicks).collect(&:to_i).max
          avg_number_mouseclicks = (scene_informations.pluck(:number_of_clicks).collect(&:to_i).reduce(:+).to_f /  scene_informations.size)
          number_of_clicks += scene_informations.pluck(:number_of_clicks)
          time_mouse_down += scene_informations.pluck(:time_of_mouse_down)
          min_time_mouse_down    = scene_informations.pluck(:time_of_mouse_down).collect(&:to_i).min
          max_time_mouse_down    = scene_informations.pluck(:time_of_mouse_down).collect(&:to_i).max
          avg_time_mouse_down    = (scene_informations.pluck(:time_of_mouse_down).collect(&:to_i).reduce(:+).to_f /  scene_informations.size)
          
          ### the answers for the scene
          scene_step_participant_experiment_steps_answers = step_participant_experiment_steps_answers.where(:participant_experiment_step_id => scene_informations.pluck(:participant_experiment_step_id))
          {
            :scene_name => scene.name,
            :url => scene.url,
            :viewed_by_number_of_participants => scene_informations.collect{|si| si.participant_experiment_step.participant}.uniq.length,
            :viewed_by_percentage_of_participants => scene_informations.collect{|si| si.participant_experiment_step.participant}.uniq.length.to_f / step_participant_experiment_steps_scenes.collect{|st| st.participant_experiment_step.participant}.length * 100,
            :min_number_mouseclicks => min_number_mouseclicks, 
            :max_number_mouseclicks => max_number_mouseclicks, 
            :avg_number_mouseclicks => avg_number_mouseclicks, 
            :min_time_mouse_down    => min_time_mouse_down,    
            :max_time_mouse_down    => max_time_mouse_down,    
            :avg_time_mouse_down    => avg_time_mouse_down,
            :questions => Question.where(:id => scene_step_participant_experiment_steps_answers.pluck(:question_id)).collect{|question|
              question_answers = scene_step_participant_experiment_steps_answers.where(:question_id => question.id)
              all_given_answers = question_answers.all.collect{|qa| qa.answers}.flatten
              max_time = question_answers.pluck(:time_needed_in_ms).collect(&:to_i).max
              min_time = question_answers.pluck(:time_needed_in_ms).collect(&:to_i).min
              avg_time = (question_answers.pluck(:time_needed_in_ms).collect(&:to_i).reduce(:+).to_f / question_answers.size)
              {
                :question_id => question.id,
                :question_text => question.question_text,
                :all_given_answers => all_given_answers,
                :answered_correct => question_answers.where(:answered_correctly => true).size,
                :answered_by_number_of_participants => question_answers.collect{|qa| qa.participant_experiment_step.participant}.uniq.length,
                :percent_correct => (question_answers.where(:answered_correctly => true).size / question_answers.collect{|qa| qa.participant_experiment_step.participant}.uniq.length.to_f ) * 100.0,
                :participants_answers => question_answers.collect{|qa|
                  {:participant_id => qa.participant_experiment_step.participant.id, :answer => qa.answer, :correct => qa.answered_correctly, :time_needed => qa.time_needed_in_ms, :mistakes => qa.mistakes}
                },
                :time_needed_max => max_time,
                :time_needed_min => min_time,
                :time_needed_avg => avg_time
              }
            }
          }
          
        }
        experiment_step_result[:sum_result] = {}
        experiment_step_result[:sum_result][:scenes] = {
          :min_number_mouseclicks => number_of_clicks.collect(&:to_i).min,                                          
          :max_number_mouseclicks => number_of_clicks.collect(&:to_i).max,                                          
          :avg_number_mouseclicks => number_of_clicks.collect(&:to_i).reduce(:+).to_f / number_of_clicks.length.to_f, 
          :min_time_mouse_down    => time_mouse_down.collect(&:to_i).min,                                          
          :max_time_mouse_down    => time_mouse_down.collect(&:to_i).max,                                          
          :avg_time_mouse_down    => time_mouse_down.collect(&:to_i).reduce(:+).to_f / time_mouse_down.length.to_f,
          :sum_participants       => step_participant_experiment_steps_scenes.collect{|st| st.participant_experiment_step.participant}.uniq.length.to_f
        }


        experiment_step_result[:result][:questions] = Question.where(:id => step_participant_experiment_steps_answers.pluck(:question_id)).collect{|question|
          question_result = {
            :question_id => question.id,
            :question_text => question.question_text,
            :question_has_correct_answer => question.has_correct_answer?,
            :per_scene => {}
          }
          ([nil] + Scene.where(:id => step_participant_experiment_steps_scenes.pluck(:scene_id))).each{|scene|
            
            question_answers = step_participant_experiment_steps_answers.where(:question_id => question.id)
            if scene
              question_answers = question_answers.joins(:participant_experiment_step => [:participant_experiment_step_scenes]).where(ParticipantExperimentStepScene.arel_table[:scene_id].eq(scene.id))
            end
            
            all_given_answers = question_answers.all.collect{|qa| qa.answers}.flatten
            max_time = question_answers.pluck(:time_needed_in_ms).collect(&:to_i).max
            min_time = question_answers.pluck(:time_needed_in_ms).collect(&:to_i).min
            avg_time = (question_answers.pluck(:time_needed_in_ms).collect(&:to_i).reduce(:+).to_f / question_answers.size)
            
            correct_answers = question_answers.where(:answered_correctly => true)
            max_time_correct = correct_answers.pluck(:time_needed_in_ms).collect(&:to_i).max
            min_time_correct = correct_answers.pluck(:time_needed_in_ms).collect(&:to_i).min
            avg_time_correct = (correct_answers.pluck(:time_needed_in_ms).collect(&:to_i).reduce(:+).to_f / correct_answers.size)
            
            incorrect_answers = question_answers.where(:answered_correctly => [false, nil])
            max_time_incorrect = incorrect_answers.pluck(:time_needed_in_ms).collect(&:to_i).max
            min_time_incorrect = incorrect_answers.pluck(:time_needed_in_ms).collect(&:to_i).min
            avg_time_incorrect = (incorrect_answers.pluck(:time_needed_in_ms).collect(&:to_i).reduce(:+).to_f / incorrect_answers.size)
            
            min_mistakes = question_answers.pluck(:mistakes).collect(&:to_i).min
            max_mistakes = question_answers.pluck(:mistakes).collect(&:to_i).max
            avg_mistakes = (question_answers.pluck(:mistakes).collect(&:to_i).reduce(:+).to_f / question_answers.size)
            
            min_mistakes_correct = correct_answers.pluck(:mistakes).collect(&:to_i).min
            max_mistakes_correct = correct_answers.pluck(:mistakes).collect(&:to_i).max
            avg_mistakes_correct = (correct_answers.pluck(:mistakes).collect(&:to_i).reduce(:+).to_f / correct_answers.size)
            
            question_result[:per_scene][scene ? scene.id : :all] = {
              :all_given_answers => all_given_answers,
              :answered_correct => question_answers.where(:answered_correctly => true).size,
              :answered_by_number_of_participants => question_answers.collect{|qa| qa.participant_experiment_step.participant}.uniq.length,
              :percent_correct => (question_answers.collect{|qa| qa.participant_experiment_step.participant}.uniq.length.to_f / question_answers.where(:answered_correctly => true).size) * 100.0,
              :participants_answers => question_answers.collect{|qa|
                {:participant_id => qa.participant_experiment_step.participant.id, :answer => qa.answer, :correct => qa.answered_correctly, :time_needed => qa.time_needed_in_ms, :mistakes => qa.mistakes}
              },
              :time_needed_max => max_time,
              :time_needed_min => min_time,
              :time_needed_avg => avg_time,
              :time_needed_max_correct => max_time_correct,
              :time_needed_min_correct => min_time_correct,
              :time_needed_avg_correct => avg_time_correct,
              :time_needed_max_incorrect => max_time_incorrect,
              :time_needed_min_incorrect => min_time_incorrect,
              :time_needed_avg_incorrect => avg_time_incorrect,
              :min_mistakes => min_mistakes,
              :max_mistakes => max_mistakes,
              :avg_mistakes => avg_mistakes,
              :min_mistakes_correct => min_mistakes_correct,
              :max_mistakes_correct => max_mistakes_correct,
              :avg_mistakes_correct => avg_mistakes_correct
            }
          }
          question_result
        }

      else
        puts "ES with questions without scene #{id}"
        experiment_step_result[:step_type] = :questions
        experiment_step_result[:result] = Question.where(:id => step_participant_experiment_steps_answers.pluck(:question_id)).collect{|question|
          question_answers = step_participant_experiment_steps_answers.where(:question_id => question.id)
          all_given_answers = question_answers.all.collect{|qa| qa.answers}.flatten
          max_time = question_answers.pluck(:time_needed_in_ms).collect(&:to_i).max
          min_time = question_answers.pluck(:time_needed_in_ms).collect(&:to_i).min
          avg_time = (question_answers.pluck(:time_needed_in_ms).collect(&:to_i).reduce(:+).to_f / question_answers.size)

          correct_answers = question_answers.where(:answered_correctly => true)
          max_time_correct = correct_answers.pluck(:time_needed_in_ms).collect(&:to_i).max
          min_time_correct = correct_answers.pluck(:time_needed_in_ms).collect(&:to_i).min
          avg_time_correct = (correct_answers.pluck(:time_needed_in_ms).collect(&:to_i).reduce(:+).to_f / correct_answers.size)

          incorrect_answers = question_answers.where(:answered_correctly => [false, nil])
          max_time_incorrect = incorrect_answers.pluck(:time_needed_in_ms).collect(&:to_i).max
          min_time_incorrect = incorrect_answers.pluck(:time_needed_in_ms).collect(&:to_i).min
          avg_time_incorrect = (incorrect_answers.pluck(:time_needed_in_ms).collect(&:to_i).reduce(:+).to_f / incorrect_answers.size)

          min_mistakes = question_answers.pluck(:mistakes).collect(&:to_i).min
          max_mistakes = question_answers.pluck(:mistakes).collect(&:to_i).max
          avg_mistakes = (question_answers.pluck(:mistakes).collect(&:to_i).reduce(:+).to_f / question_answers.size)

          min_mistakes_correct = correct_answers.pluck(:mistakes).collect(&:to_i).min
          max_mistakes_correct = correct_answers.pluck(:mistakes).collect(&:to_i).max
          avg_mistakes_correct = (correct_answers.pluck(:mistakes).collect(&:to_i).reduce(:+).to_f / correct_answers.size)
          

          
          {
            :question_id => question.id,
            :question_text => question.question_text,
            :question_has_correct_answer => question.has_correct_answer?,
            :all_given_answers => all_given_answers,
            :answered_correct => question_answers.where(:answered_correctly => true).size,
            :answered_by_number_of_participants => question_answers.collect{|qa| qa.participant_experiment_step.participant}.uniq.length,
            :percent_correct => (question_answers.where(:answered_correctly => true).size) / (question_answers.collect{|qa| qa.participant_experiment_step.participant}.uniq.length.to_f) * 100.0,
            :participants_answers => question_answers.collect{|qa|
              {:participant_id => qa.participant_experiment_step.participant.id, :answer => qa.answer, :correct => qa.answered_correctly, :time_needed => qa.time_needed_in_ms, :mistakes => qa.mistakes}
            },
            :time_needed_max => max_time,
            :time_needed_min => min_time,
            :time_needed_avg => avg_time,
            :time_needed_max_correct => max_time_correct,
            :time_needed_min_correct => min_time_correct,
            :time_needed_avg_correct => avg_time_correct,
            :time_needed_max_incorrect => max_time_incorrect,
            :time_needed_min_incorrect => min_time_incorrect,
            :time_needed_avg_incorrect => avg_time_incorrect,
            :min_mistakes => min_mistakes,
            :max_mistakes => max_mistakes,
            :avg_mistakes => avg_mistakes,
            :min_mistakes_correct => min_mistakes_correct,
            :max_mistakes_correct => max_mistakes_correct,
            :avg_mistakes_correct => avg_mistakes_correct,
          }
        }
      end
    elsif step_participant_experiment_steps_scenes.size > 0
      puts "ES without questions with scene"
      experiment_step_result[:step_type] = :scene

      number_of_clicks = []
      time_mouse_down = []
      
      experiment_step_result[:result] = Scene.where(:id => step_participant_experiment_steps_scenes.pluck(:scene_id)).collect{|scene|
        scene_informations = step_participant_experiment_steps_scenes.where(:scene_id => scene)
        number_of_clicks += scene_informations.pluck(:number_of_clicks)
        time_mouse_down += scene_informations.pluck(:time_of_mouse_down)
        min_number_mouseclicks = scene_informations.pluck(:number_of_clicks).collect(&:to_i).min
        max_number_mouseclicks = scene_informations.pluck(:number_of_clicks).collect(&:to_i).max
        avg_number_mouseclicks = (scene_informations.pluck(:number_of_clicks).collect(&:to_i).reduce(:+).to_f /  scene_informations.size)
        min_time_mouse_down    = scene_informations.pluck(:time_of_mouse_down).collect(&:to_i).min
        max_time_mouse_down    = scene_informations.pluck(:time_of_mouse_down).collect(&:to_i).max
        avg_time_mouse_down    = (scene_informations.pluck(:time_of_mouse_down).collect(&:to_i).reduce(:+).to_f /  scene_informations.size)
        
        {
          :scene_name => scene.name,
          :url => scene.url,
          :viewed_by_number_of_participants => scene_informations.collect{|si| si.participant_experiment_step.participant}.uniq.length,
          :viewed_by_percentage_of_participants => scene_informations.collect{|si| si.participant_experiment_step.participant}.uniq.length.to_f / step_participant_experiment_steps_scenes.collect{|st| st.participant_experiment_step.participant}.uniq.length.to_f  * 100,
          :min_number_mouseclicks => min_number_mouseclicks, 
          :max_number_mouseclicks => max_number_mouseclicks, 
          :avg_number_mouseclicks => avg_number_mouseclicks, 
          :min_time_mouse_down    => min_time_mouse_down,    
          :max_time_mouse_down    => max_time_mouse_down,    
          :avg_time_mouse_down    => avg_time_mouse_down    
        }
      }
      experiment_step_result[:sum_result] = {
        :min_number_mouseclicks => number_of_clicks.collect(&:to_i).min,                                          
        :max_number_mouseclicks => number_of_clicks.collect(&:to_i).max,                                          
        :avg_number_mouseclicks => number_of_clicks.collect(&:to_i).reduce(:+).to_f / number_of_clicks.length.to_f, 
        :min_time_mouse_down    => time_mouse_down.collect(&:to_i).min,                                          
        :max_time_mouse_down    => time_mouse_down.collect(&:to_i).max,                                          
        :avg_time_mouse_down    => time_mouse_down.collect(&:to_i).reduce(:+).to_f / time_mouse_down.length.to_f,
        :sum_participants       => step_participant_experiment_steps_scenes.collect{|st| st.participant_experiment_step.participant}.uniq.length.to_f
        

      }
      
    else
      experiment_step_result[:step_type] = :text
      puts "ES without questions without scene"
    end

    return experiment_step_result

  end


  def step_results_per_scene
    step_results = {}
    if step.is_a?(GroupedStep)
      
    else
      if step.scenes.empty?
        
        
      else
      
      end
      
    end

    return step_results
  end

  def scene_results
    all_participant_experiment_steps = ParticipantExperimentStep.joins(:experiment_step => [:experiment]).where(Experiment.arel_table[:id].eq(self.experiment_id))
    all_participant_experiment_steps_answers = ParticipantExperimentStepAnswer.joins(:participant_experiment_step => [:experiment_step => [:experiment]]).where(Experiment.arel_table[:id].eq(self.experiment_id))
    all_participant_experiment_steps_scenes = ParticipantExperimentStepScene.joins(:participant_experiment_step => [:experiment_step => [:experiment]]).where(Experiment.arel_table[:id].eq(self.experiment_id))
    
    step_participant_experiment_steps = all_participant_experiment_steps.where(ExperimentStep.arel_table[:id].eq(id))

    scenes = []
    
    step_participant_experiment_steps.each{|pes|
      scenes += pes.scenes
    }
    scenes.uniq!


    interaction_per_scene = {}
    for scene in scenes

      
      

    end
    
    return {
      :scenes => scenes

    }

    
    
    

  end
  

  
end
