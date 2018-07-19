FactoryBot.define do
  factory :text do
    title {Faker::StarTrek.character}
    description {Faker::StarTrek.location}
    html {"<div>#{ Faker::TheFreshPrinceOfBelAir.quote}</div>"}
  end
end
