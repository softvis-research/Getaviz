class Participant < ApplicationRecord
  belongs_to :experiment
  has_many :participant_experiment_steps, :dependent => :destroy
  
  before_save :default_values
  def default_values
    self.id_hash ||= SecureRandom.uuid
  end


end
