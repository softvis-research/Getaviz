class CreateExperiments < ActiveRecord::Migration[4.2]
  def change
    create_table :experiments do |t|
      t.string :title
      t.text :description
      t.string :id_hash
      t.references :greeting, index: true
      t.references :farewell, index: true
      t.references :post_test, index: true
      t.references :pre_test, index: true
      t.timestamps
    end
  end
end
