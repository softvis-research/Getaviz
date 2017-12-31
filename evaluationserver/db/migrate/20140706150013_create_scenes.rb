class CreateScenes < ActiveRecord::Migration[4.2]
  def change
    create_table :scenes do |t|
      t.string :name
      t.string :description
      t.attachment :scenefile
      t.timestamps
    end
  end
end
