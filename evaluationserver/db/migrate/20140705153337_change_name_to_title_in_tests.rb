class ChangeNameToTitleInTests < ActiveRecord::Migration[4.2]
  def change
    rename_column :tests, :name, :title
  end
end
