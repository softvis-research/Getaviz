require 'rails_helper'

RSpec.feature "create experiment", type: :feature do
  scenario "without authorization" do
    visit new_experiment_path
    expect(page).not_to have_content(t('experiments.new'))
  end

  scenario "with authorization" do
    authenticate_as_admin
    visit new_experiment_path
    expect(page).to have_content(t('experiments.new'))
    title = Faker::Name.last_name
    description = Faker::TheFreshPrinceOfBelAir.quote
    fill_in 'experiment_title', with: title 
    fill_in 'experiment_description', with: description
    find('input[name="commit"]').click
    expect(Experiment.last.title).to eq(title)
    expect(Experiment.last.description).to eq(description)
  end


  
end
