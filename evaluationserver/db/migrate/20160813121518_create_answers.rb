class CreateAnswers < ActiveRecord::Migration[5.0]
  def change
    create_table :answers do |t|
      t.references :answer_possibility_collection, foreign_key: true
      t.string :answer_text
      t.string :placeholder
      t.float :min
      t.float :max
      t.float :step
      t.string :default
      t.string :correct_answer_string
      t.float :correct_answer_value_min
      t.float :correct_answer_value_max
      t.boolean :is_correct_choice
      
      
      t.timestamps
    end
  end
end
