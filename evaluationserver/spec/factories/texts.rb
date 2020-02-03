FactoryBot.define do
  factory :text do
    title {Faker::TvShows::StarTrek.character}
    description {Faker::TvShows::StarTrek.location}
    html {"<div>#{ Faker::TvShows::TheFreshPrinceOfBelAir.quote}</div>"}
  end
end
