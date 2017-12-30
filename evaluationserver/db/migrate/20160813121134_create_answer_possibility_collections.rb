class CreateAnswerPossibilityCollections < ActiveRecord::Migration[5.0]
  def change
    create_table :answer_possibility_collections do |t|
      t.references :question, foreign_key: true
      t.string :answer_type
      t.integer :timeout
      
      t.timestamps
    end
  end
end
