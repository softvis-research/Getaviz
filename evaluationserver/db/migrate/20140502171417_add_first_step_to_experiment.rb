class AddFirstStepToExperiment < ActiveRecord::Migration
  def change
    add_reference :experiments, :first_step, index: true
  end
end
