class CreateQuestionaireQuestions < ActiveRecord::Migration[5.0]
  def change
    create_table :questionaire_questions do |t|
      t.references :questionaire
      t.references :question
      t.integer :position
      t.timestamps
    end
  end
end
