FactoryBot.define do
  factory :question do
    factory :number_question do
      question_text {"Welche Nummer wird gesucht?"}

      factory :number_question_with_one_answer_possibility do
        after(:create) do |number_question_with_one_answer_possibility, evaluator|
          create_list(:answer_possibility_collection, 1, question: number_question_with_one_answer_possibility)
        end
      end
      
      
      
    end

    factory :text_question do
      question_text {"Welcher Text wird gesucht?"}
      
    end
    
    
  end
end
