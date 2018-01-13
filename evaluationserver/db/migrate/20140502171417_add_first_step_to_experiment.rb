class AddFirstStepToExperiment < ActiveRecord::Migration[4.2]
  def change
    add_reference :experiments, :first_step, index: true
  end
end
