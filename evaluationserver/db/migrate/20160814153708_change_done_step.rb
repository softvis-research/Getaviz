class ChangeDoneStep < ActiveRecord::Migration[4.2][5.0]
  def change
    rename_table :done_steps, :participant_experiment_steps
    add_column :participant_experiment_steps, :id_hash, :string
    add_column :participant_experiment_steps, :started, :datetime
    add_column :participant_experiment_steps, :done, :datetime
    
    rename_table :participant_answers, :participant_experiment_step_answers
    rename_table :participant_scenes, :participant_experiment_step_scenes
    

    
  end
end
