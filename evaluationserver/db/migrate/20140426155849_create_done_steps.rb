class CreateDoneSteps < ActiveRecord::Migration[4.2]
  def change
    create_table :done_steps do |t|
      t.references :experiment_step, index: true
      t.references :participant, index: true

      t.timestamps
    end
  end
end
