class AddColumnNameToTexts < ActiveRecord::Migration[4.2]
  def change
    add_column :texts, :title, :string
  end
end
