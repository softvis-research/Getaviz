class CreateParticipantAnswers < ActiveRecord::Migration[4.2]
  def change
    create_table :participant_answers do |t|
      t.text :answer
      t.references :participant, index: true
      t.references :question, index: true

      t.timestamps
    end
  end
end
