class ChangeExperimentSteps < ActiveRecord::Migration[4.2][5.0]
  def change
    remove_column :experiment_steps, :stepable_id
    remove_column :experiment_steps, :stepable_type
  end
end
