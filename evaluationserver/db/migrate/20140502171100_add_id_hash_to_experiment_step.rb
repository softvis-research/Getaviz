class AddIdHashToExperimentStep < ActiveRecord::Migration[4.2]
  def change
    add_column :experiment_steps, :id_hash, :string
  end
end
