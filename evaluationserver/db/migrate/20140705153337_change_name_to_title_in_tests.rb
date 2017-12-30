class ChangeNameToTitleInTests < ActiveRecord::Migration
  def change
    rename_column :tests, :name, :title
  end
end
