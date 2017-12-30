class Questionaire < TextualElement
  has_many :questionaire_questions
  has_many :questions, ->{ order('questionaire_questions.position, questionaire_questions.id')}, :through => :questionaire_questions
  
end
