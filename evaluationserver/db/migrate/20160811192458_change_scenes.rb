class ChangeScenes < ActiveRecord::Migration[4.2][5.0]
  def change
    remove_column :scenes, :scenefile_file_name
    remove_column :scenes, :scenefile_content_type
    remove_column :scenes, :scenefile_file_size
    remove_column :scenes, :scenefile_updated_at
    remove_column :scenes, :use_url
  end
end
