class CreateTests < ActiveRecord::Migration
  def change
    create_table :tests do |t|
      t.string :title
      t.text :description
      t.string :filename

      t.timestamps
    end
  end
end
