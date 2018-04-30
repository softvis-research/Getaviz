require 'rails_helper'

RSpec.feature "conduct experiment", type: :feature do
  
  scenario "with static text" do
    @experiment = create(:experiment_with_static_text)
    visit start_path(:experiment_hash => @experiment.id_hash)
    expect(page).to have_content(Nokogiri::XML(@experiment.steps.first.textual_elements.first.html).content)
    find('input[name="commit"]').click
  end

  scenario "with scene" do


  end

  scenario "with scene test" do

  end

  scenario "with multiple scenes test" do

  end

  
end
