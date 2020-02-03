FactoryBot.define do
  factory :step do
    title {Faker::TvShows::StarTrek.character}
    description {Faker::TvShows::TheFreshPrinceOfBelAir.quote}
    type {"Step"}
    factory :step_with_static_text do
      after(:create) do |step, evaluator|
        text = create(:text)
        step.textual_elements << text
      end
    end
  end
end
