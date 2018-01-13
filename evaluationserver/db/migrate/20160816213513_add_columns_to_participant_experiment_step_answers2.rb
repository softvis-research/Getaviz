class AddColumnsToParticipantExperimentStepAnswers2 < ActiveRecord::Migration[4.2][5.0]
  def change
    add_column :participant_experiment_step_answers, :answered_correctly, :boolean
  end
end
