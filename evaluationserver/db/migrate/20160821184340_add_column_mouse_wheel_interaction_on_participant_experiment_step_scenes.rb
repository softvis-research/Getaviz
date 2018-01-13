class AddColumnMouseWheelInteractionOnParticipantExperimentStepScenes < ActiveRecord::Migration[4.2][5.0]
  def change
    add_column :participant_experiment_step_scenes, :mouse_wheel_interaction, :integer, :default => 0
  end
end
