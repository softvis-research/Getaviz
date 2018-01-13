class CreateTextualElements < ActiveRecord::Migration[4.2][5.0]
  def change
    create_table :textual_elements do |t|
      t.string :title
      t.string :description
      t.text :html
      t.integer :questionaire_type_id
      t.integer :timeout
      t.timestamps
    end

    
    
  end
end
