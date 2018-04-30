FactoryBot.define do
  factory :experiment do
    title {Faker::StarTrek.character}
    description {Faker::TheFreshPrinceOfBelAir.quote}

    factory :experiment_with_static_text do

      after(:create) do |experiment, evaluator|
        step = create(:step_with_static_text)
        experiment.steps << step
      end
      
      factory :experiment_with_scene do


        factory :experiment_with_scene_test do
        
        end
      end

      
    end

    

    

    
  end
end
