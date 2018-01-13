class AddColumnsToParticipantExperimentStepAnswer < ActiveRecord::Migration[4.2][5.0]
  def change
    add_column :participant_experiment_step_answers, :answers, :text
  end
end
