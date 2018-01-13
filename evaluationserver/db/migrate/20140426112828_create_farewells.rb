class CreateFarewells < ActiveRecord::Migration[4.2]
  def change
    create_table :farewells do |t|
      t.text :text

      t.timestamps
    end
  end
end
