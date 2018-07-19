require 'rails_helper'

RSpec.describe "Experiments", type: :request do
  describe "GET /experiments" do
    it "works! (now write some real specs)" do
      get experiments_path
      expect(response).to have_http_status(200)
    end
  end

  describe "GET /experiment/:id" do
    it "works! (now write some real specs)" do
      get experiments_path
      expect(response).to have_http_status(200)
    end
  end
end
