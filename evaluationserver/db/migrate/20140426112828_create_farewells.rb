class CreateFarewells < ActiveRecord::Migration
  def change
    create_table :farewells do |t|
      t.text :text

      t.timestamps
    end
  end
end
