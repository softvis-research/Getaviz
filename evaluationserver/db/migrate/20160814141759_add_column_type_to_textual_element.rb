class AddColumnTypeToTextualElement < ActiveRecord::Migration[5.0]
  def change
    add_column :textual_elements, :type, :string
  end
end
