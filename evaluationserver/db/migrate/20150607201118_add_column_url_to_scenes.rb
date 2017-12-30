class AddColumnUrlToScenes < ActiveRecord::Migration
  def change
    add_column :scenes, :url, :string
    add_column :scenes, :use_url, :boolean, :default => false
  end
end
