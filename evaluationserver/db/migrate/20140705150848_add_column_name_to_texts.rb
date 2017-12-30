class AddColumnNameToTexts < ActiveRecord::Migration
  def change
    add_column :texts, :title, :string
  end
end
