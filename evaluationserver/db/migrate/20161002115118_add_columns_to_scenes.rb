class AddColumnsToScenes < ActiveRecord::Migration[4.2][5.0]
  def change
    add_column :scenes, :remarks, :text
    add_column :scenes, :mapping, :text
  end
end
