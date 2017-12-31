class AddReferencesToSceneTest < ActiveRecord::Migration[4.2]
  def change
    add_reference :scene_tests, :assigner, index: true
  end
end
