class AddReferencesToSceneTest < ActiveRecord::Migration
  def change
    add_reference :scene_tests, :assigner, index: true
  end
end
