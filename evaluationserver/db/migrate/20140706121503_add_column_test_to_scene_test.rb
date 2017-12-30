class AddColumnTestToSceneTest < ActiveRecord::Migration
  def change
    add_reference :scene_tests, :test, index: true
  end
end
