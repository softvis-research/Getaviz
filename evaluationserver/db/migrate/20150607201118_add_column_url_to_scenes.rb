class AddColumnUrlToScenes < ActiveRecord::Migration[4.2]
  def change
    add_column :scenes, :url, :string
    add_column :scenes, :use_url, :boolean, :default => false
  end
end
