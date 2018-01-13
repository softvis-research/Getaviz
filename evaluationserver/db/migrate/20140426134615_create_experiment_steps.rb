class CreateExperimentSteps < ActiveRecord::Migration[4.2]
  def change
    create_table :experiment_steps do |t|
      t.integer :number
      t.references :stepable, polymorphic: true, index: true
      t.references :experiment, index: true

      t.timestamps
    end
  end
end
