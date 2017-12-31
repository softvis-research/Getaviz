class CreateTexts < ActiveRecord::Migration[4.2]
  def change
    create_table :texts do |t|
      t.text :html

      t.timestamps
    end
  end
end
