class ParticipantExperimentStepAnswer < ActiveRecord::Base
  belongs_to :participant_experiment_step
  belongs_to :question

  def answer=(given_answer)
    if self.answers
      the_answers = self.answers
      the_answers << given_answer
    else
      the_answers = [given_answer]
    end
    self.answers = the_answers.to_yaml
    super(given_answer)
  end
  def answers
    tmp_answers = read_attribute(:answers)
    return [] unless tmp_answers
    YAML.load(tmp_answers)
  end

  def related_scene
    participant_experiment_step.get_participant_scene
  end

  def participant
    participant_experiment_step.participant
  end

  def given_answer_cleartext
    if self.answer and self.answer.strip =~ /^\[.*\]$/
    ## is an choice
      array = JSON.parse(self.answer)
      array.collect!{|a| Answer.find(a.to_i).answer_text}
      return array.join(";")
    else
      return self.answer
    end
  end


  def number_of_mistakes_in_final_answer
    correct_answer = question.correct_answer
    if correct_answer.is_a?(Array)
      answer_array = JSON.parse(self.answer)
      answer_array.collect!{|a| Answer.find(a.to_i).answer_text}
      return (correct_answer - answer_array).length
    else
      if self.answer == correct_answer
        return 0
      else
        return 1
      end
    end
  end
  
  
end
