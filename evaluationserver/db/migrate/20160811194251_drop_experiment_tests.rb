class DropExperimentTests < ActiveRecord::Migration[4.2][5.0]
  def change
    drop_table :experiment_tests
  end
end
