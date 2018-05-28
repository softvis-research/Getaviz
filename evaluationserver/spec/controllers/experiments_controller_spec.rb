require 'rails_helper'

RSpec.describe ExperimentsController, type: :controller do
  include BasicAuthHelper
  describe "GET conduct" do
    it "Raises controlled error on wrong experiment" do
      @experiment = create(:experiment_with_static_text)
      get 'conduct', :params => {:id_hash => @experiment.id_hash} ## this is the wrong type of hash
      expect(response).to have_http_status(:error)
    end
  end

  describe "GET edit" do
    it "opens edit page" do
      login_as_admin
      @experiment = create(:experiment_with_static_text)
      get 'edit', :params => {:id => @experiment.id} ## this is the wrong type of hash
      expect(response).to have_http_status(:ok)
    end
  end


  describe "POST update" do
    it "can update the title" do
      login_as_admin
      @experiment = create(:experiment_with_static_text)
      new_title = Faker::Name.name
      patch 'update', :params => {:id => @experiment.id, :experiment => {:title => new_title}} ## this is the wrong type of hash
      expect(response).to have_http_status(:found)
      @experiment.reload
      expect(@experiment.title).to eq(new_title)
      
    end
  end


  describe "DELETE destroy" do
    it "can update the title" do
      login_as_admin
      @experiment = create(:experiment_with_static_text)
      experiment_id = @experiment.id
      expect(Experiment.where(:id => experiment_id).length).to eq(1)
      
      new_title = Faker::Name.name
      delete 'destroy', :params => {:id => @experiment.id} ## this is the wrong type of hash
      expect(response).to have_http_status(:found)
      expect(Experiment.where(:id => experiment_id).length).to eq(0)
      
    end
  end





  
end
