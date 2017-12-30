class CreateQuestionOptions < ActiveRecord::Migration
  def change
    create_table :question_options do |t|
      t.string :answer
      t.references :question, index: true

      t.timestamps
    end
  end
end
