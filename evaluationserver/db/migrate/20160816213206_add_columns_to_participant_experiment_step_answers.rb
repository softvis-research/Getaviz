class AddColumnsToParticipantExperimentStepAnswers < ActiveRecord::Migration[5.0]
  def change
    add_column :participant_experiment_step_answers, :mistakes, :integer
    add_column :participant_experiment_step_answers, :helps, :integer
  end
end
