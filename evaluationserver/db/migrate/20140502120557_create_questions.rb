class CreateQuestions < ActiveRecord::Migration[4.2]
  def change
    create_table :questions do |t|
      t.text :question_text
      t.references :question_type, index: true

      t.timestamps
    end
  end
end
