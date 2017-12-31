class AddColumnTestToSceneTest < ActiveRecord::Migration[4.2]
  def change
    add_reference :scene_tests, :test, index: true
  end
end
