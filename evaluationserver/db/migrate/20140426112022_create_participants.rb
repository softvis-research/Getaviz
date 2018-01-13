class CreateParticipants < ActiveRecord::Migration[4.2]
  def change
    create_table :participants do |t|
      t.string :id_hash
      t.integer :experiment_id
      t.timestamps
    end
  end
end
