class ChangeParticipantExperimentStepScene < ActiveRecord::Migration[5.0]
  def change
    remove_column :participant_experiment_step_scenes, :participant_id
    add_column :participant_experiment_step_scenes, :participant_experiment_step_id, :integer
  end
end
