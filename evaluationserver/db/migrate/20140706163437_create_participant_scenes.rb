class CreateParticipantScenes < ActiveRecord::Migration[4.2]
  def change
    create_table :participant_scenes do |t|
      t.references :participant, index: true
      t.references :scene, index: true

      t.timestamps
    end
  end
end
