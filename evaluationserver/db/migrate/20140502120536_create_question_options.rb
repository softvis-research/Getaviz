class CreateQuestionOptions < ActiveRecord::Migration[4.2]
  def change
    create_table :question_options do |t|
      t.string :answer
      t.references :question, index: true

      t.timestamps
    end
  end
end
