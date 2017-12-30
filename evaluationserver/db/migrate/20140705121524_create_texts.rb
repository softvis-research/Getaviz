class CreateTexts < ActiveRecord::Migration
  def change
    create_table :texts do |t|
      t.text :html

      t.timestamps
    end
  end
end
