class CreateSceneTestScenes < ActiveRecord::Migration[4.2]
  def change
    create_table :scene_test_scenes do |t|
      t.references :scene_test, index: true
      t.references :scene, index: true

      t.timestamps
    end
  end
end
