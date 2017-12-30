class CreateScenes < ActiveRecord::Migration
  def change
    create_table :scenes do |t|
      t.string :name
      t.string :description
      t.attachment :scenefile
      t.timestamps
    end
  end
end
