class CreateTestQuestions < ActiveRecord::Migration[4.2]
  def change
    create_table :test_questions do |t|
      t.references :test, :index => true
      t.references :question, :index => true
      t.integer :position
      t.timestamps
    end
  end
end
