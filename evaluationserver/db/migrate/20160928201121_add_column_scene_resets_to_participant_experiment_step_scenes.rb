class AddColumnSceneResetsToParticipantExperimentStepScenes < ActiveRecord::Migration[4.2][5.0]
  def change
    add_column :participant_experiment_step_scenes, :number_of_resets, :integer
  end
end
