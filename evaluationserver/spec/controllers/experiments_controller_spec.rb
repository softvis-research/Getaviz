require 'rails_helper'

RSpec.describe ExperimentsController, type: :controller do
  describe "GET conduct" do
    it "Raises controlled error on wrong experiment" do
      @experiment = create(:experiment_with_static_text)
      get 'conduct', :params => {:id_hash => @experiment.id_hash} ## this is the wrong type of hash
      expect(response).to have_http_status(:error)
    end
  end
end
