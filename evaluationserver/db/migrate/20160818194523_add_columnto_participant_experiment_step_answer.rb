class AddColumntoParticipantExperimentStepAnswer < ActiveRecord::Migration[5.0]
  def change
    add_column :participant_experiment_step_answers, :participant_experiment_step_id, :integer
    remove_column :participant_experiment_step_answers, :participant_id
  end
end
