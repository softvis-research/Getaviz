class AddColumnsToParticipantExperimentStepScenes < ActiveRecord::Migration[4.2][5.0]
  def change
    add_column :participant_experiment_step_scenes, :log_hash, :text
    add_column :participant_experiment_step_scenes, :number_of_clicks, :integer
    add_column :participant_experiment_step_scenes, :time_of_mouse_down, :integer
  end
end
