class AnswerPossibilityCollection < ApplicationRecord
  belongs_to :question

  has_many :answers, :dependent => :destroy
  accepts_nested_attributes_for :answers, reject_if: :all_blank, allow_destroy: true

  def is_correct_answered_with?(given_answer)
    case self.answer_type
    when "freetext"
      return self.answers.first.is_correct_answered_with?(given_answer)
    when "slider"
      return self.answers.first.is_correct_answered_with?(given_answer)
    when "multiplechoice"
      given_answer = [] unless given_answer
      return self.answers.where(:is_correct_choice => true).pluck(:id).sort == given_answer.collect(&:to_i).sort
    when "choice"
      given_answer = [] unless given_answer
      return self.answers.where(:is_correct_choice => true).pluck(:id).sort == given_answer.collect(&:to_i).sort
    end
  end

  def has_correct_answer?
    case self.answer_type
    when "freetext"
      return (self.answers.first and not(self.answers.first.correct_answer_string.to_s.strip.empty?))
    when "slider"
      return (self.answers.first and (self.answers.first.correct_answer_value_min or self.answers.first.correct_answer_value_max))
    when "multiplechoice"
      return self.answers.where(:is_correct_choice => true).length > 0
    when "choice"
      return self.answers.where(:is_correct_choice => true).length > 0
    end

  end
  
  
end
