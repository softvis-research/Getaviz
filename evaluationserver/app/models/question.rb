class Question < ActiveRecord::Base
  #belongs_to :question_type
  has_many :questionaire_questions
  has_many :questionaires, :through => :questionaire_questions
  
  has_many :participant_answers

  has_many :answer_possibility_collections, :dependent => :destroy
  accepts_nested_attributes_for :answer_possibility_collections, reject_if: :all_blank, allow_destroy: true
  
  
  before_save :default_values
  def default_values
    self.id_hash ||= SecureRandom.uuid
  end




  def slider_to
    return options_hash[:to] if options_hash[:to]
    return 0
  end

  def slider_to=(to)
    set_options_key(:to, to)
  end


  def slider_from
    return options_hash[:from] if options_hash[:from]
    return 0
  end

  def slider_from=(from)
    set_options_key(:from, from)
  end


  def slider_granularity
    return options_hash[:granularity] if options_hash[:granularity]
    return 1
  end

  def slider_granularity=(granularity)
    set_options_key(:granularity, granularity)
  end

  def choice_text=(choices)
    set_options_key(:choices, choices)
  end

  def choice_text
    return options_hash[:choices] if options_hash[:choices]
    return []
  end

  def choices
    return choice_text
  end


  
  def options_hash
    if self.options.nil? or self.options.strip.empty?
      return {}
    else
      return YAML.load(self.options)
    end
  end

  def set_options_key(key, value)
    options_hash = self.options_hash
    options_hash[key] = value
    self.options = options_hash.to_yaml
  end


  def participant_results(experiment)
    participants = experiment.participants
    answers = participant_answers.where(participant: participants)
    result_array = []
    uniq_answers = answers.distinct(:answer).pluck(:answer).uniq
    uniq_answers.each{|uniq_answer|
      
      count = answers.where(answer: uniq_answer).length
      percentage = (count.to_f/participants.length * 100).round(2)
      result_array << {answer: uniq_answer, count: count, percentage: percentage}
    }
    result_array.sort_by!{|result|
      -result[:count]
    }
    return result_array


  end

  def has_correct_answer?
    answer_possibility_collections.find(&:has_correct_answer?)
  end

  def correct_answer_cleartext
    if has_correct_answer?
      if answer_possibility_collections.first.answer_type == "freetext"
        return answer_possibility_collections.first.answers.first.correct_answer_string
      elsif answer_possibility_collections.first.answer_type == "multiplechoice" or answer_possibility_collections.first.answer_type == "choice"
        return answer_possibility_collections.first.answers.where(:is_correct_choice => true).pluck(:answer_text).join(";")
      end
    else
      return nil
    end
  end

  def correct_answer
    if has_correct_answer?
      if answer_possibility_collections.first.answer_type == "freetext"
        return answer_possibility_collections.first.answers.first.correct_answer_string
      elsif answer_possibility_collections.first.answer_type == "multiplechoice" or answer_possibility_collections.first.answer_type == "choice"
        return answer_possibility_collections.first.answers.where(:is_correct_choice => true).pluck(:answer_text)
      end
    else
      return nil
    end
  end

end
