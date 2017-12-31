class AddColumnTypeToTextualElement < ActiveRecord::Migration[4.2][5.0]
  def change
    add_column :textual_elements, :type, :string
  end
end
