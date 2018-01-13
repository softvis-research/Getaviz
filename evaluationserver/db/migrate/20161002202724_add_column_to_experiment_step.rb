class AddColumnToExperimentStep < ActiveRecord::Migration[4.2][5.0]
  def change
    add_column :experiment_steps, :is_inner_step, :boolean, :default => false
  end
end
