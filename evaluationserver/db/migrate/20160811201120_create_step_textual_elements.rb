class CreateStepTextualElements < ActiveRecord::Migration[4.2][5.0]
  def change
    create_table :step_textual_elements do |t|
      t.references :step
      t.references :textual_element
      t.integer :position
      t.timestamps
    end
  end
end
