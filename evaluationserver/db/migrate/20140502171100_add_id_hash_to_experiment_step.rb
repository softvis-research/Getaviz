class AddIdHashToExperimentStep < ActiveRecord::Migration
  def change
    add_column :experiment_steps, :id_hash, :string
  end
end
