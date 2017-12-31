class AddAttachmentScenefileToTests < ActiveRecord::Migration[4.2]
  def self.up
    change_table :tests do |t|
      t.attachment :scenefile
    end
  end

  def self.down
    drop_attached_file :tests, :scenefile
  end
end
