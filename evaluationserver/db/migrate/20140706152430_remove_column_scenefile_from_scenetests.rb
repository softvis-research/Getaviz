class RemoveColumnScenefileFromScenetests < ActiveRecord::Migration[4.2]
  def self.up
    drop_attached_file :scene_tests, :scenefile
  end

  def self.down
    change_table :scene_tests do |t|
      t.attachment :scenefile
    end
  end

end
