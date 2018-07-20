require 'rails_helper'

RSpec.describe Step, type: :model do
  context "with only static text" do
    it "has no questions" do
      step = create(:step_with_static_text)
      expect(step.has_questions?).to be false
    end
  end
end
