class QuestionaireQuestion < ApplicationRecord
  belongs_to :questionaire
  belongs_to :question
  acts_as_list :scope => :question
end
