class AddColumnToStep < ActiveRecord::Migration[4.2][5.0]
  def change
    add_column :steps, :use_scene_from_parent_group, :boolean
  end
end
