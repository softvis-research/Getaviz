require 'rails_helper'

RSpec.feature "create experiment", type: :feature do
  scenario "without authorization" do
    visit new_experiment_path
    expect(page).not_to have_content(t('experiments.description'))
  end

  scenario "with authorization" do
    authenticate_as_admin
    visit new_experiment_path
    expect(page).to have_content(t('experiments.new'))

    screenshot_and_save_page
    
    #### Create experiment
    title = Faker::Name.last_name
    description = Faker::TvShows::TheFreshPrinceOfBelAir.quote
    fill_in 'experiment_title', with: title 
    fill_in 'experiment_description', with: description
    find('input[name="commit"]').click
    expect(Experiment.last.title).to eq(title)
    expect(Experiment.last.description).to eq(description)
    
    #### create scenes

    authenticate_as_admin
    
    visit scenes_path
    expect(page).to have_content(t('scenes.new'))

    click_link(t('scenes.new'))

    expect(page).to have_content(t('scenes.name'))
    expect(page).to have_content(t('scenes.description'))
    expect(page).to have_content(t('scenes.remarks'))
    expect(page).to have_content(t('scenes.mapping'))
    
    scene_name = Faker::Name.last_name
    scene_description = Faker::TvShows::TheFreshPrinceOfBelAir.quote
    scene_remarks = Faker::TvShows::TheFreshPrinceOfBelAir.quote
    scene_mapping = Faker::TvShows::TheFreshPrinceOfBelAir.quote
    scene_url = "http://www.example.com"

    fill_in 'scene_name', :with => scene_name
    fill_in 'scene_description', :with => scene_description
    fill_in 'scene_remarks', :with => scene_remarks
    fill_in 'scene_mapping', :with => scene_mapping
    fill_in 'scene_url', :with => scene_url

    find('input[name="commit"]').click

    expect(page).to have_content(scene_name)
    expect(page).to have_content(scene_description)
    expect(page).not_to have_content(scene_remarks)
    expect(page).not_to have_content(scene_mapping)
    expect(page).to have_content(scene_url)

    
    visit steps_path
    expect(page).to have_content(t('steps.steps'))
    expect(page).to have_content(t('new'))

    
  end


  
end
