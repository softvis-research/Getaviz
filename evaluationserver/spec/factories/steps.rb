FactoryBot.define do
  factory :step do
    title {Faker::StarTrek.character}
    description {Faker::TheFreshPrinceOfBelAir.quote}
    type {"Step"}
    factory :step_with_static_text do
      after(:create) do |step, evaluator|
        text = create(:text)
        step.textual_elements << text
      end
    end
  end
end
