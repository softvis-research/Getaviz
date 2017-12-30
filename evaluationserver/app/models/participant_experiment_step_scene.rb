class ParticipantExperimentStepScene < ActiveRecord::Base
  belongs_to :participant_experiment_step
  belongs_to :scene
end
