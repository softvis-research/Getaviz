require 'rails_helper'

RSpec.describe "CreateExperiments", type: :request do
  describe "GET /create_experiments" do
    it "works! (now write some real specs)" do
      get create_experiments_path
      expect(response).to have_http_status(200)
    end
  end
end
