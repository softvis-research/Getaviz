class ChangeColumnNumberOnExperimentStep < ActiveRecord::Migration[4.2][5.0]
  def change
    rename_column :experiment_steps, :number, :position

  end
end
