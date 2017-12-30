class AddColumnStepToExperimentStep < ActiveRecord::Migration[5.0]
  def change
    add_reference :experiment_steps, :step, foreign_key: true
  end
end
