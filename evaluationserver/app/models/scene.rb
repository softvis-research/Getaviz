class Scene < ActiveRecord::Base

  has_many :participant_scenes
  has_many :participants, :through => :participant_scenes
  before_save :default_values

  def default_values
    self.id_hash ||= SecureRandom.uuid
  end

end
