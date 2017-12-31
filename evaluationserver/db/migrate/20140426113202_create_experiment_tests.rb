class CreateExperimentTests < ActiveRecord::Migration[4.2]
  def change
    create_table :experiment_tests do |t|
      t.references :experiment, index: true
      t.references :test, index: true

      t.timestamps
    end
  end
end
