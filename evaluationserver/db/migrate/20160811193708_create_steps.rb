class CreateSteps < ActiveRecord::Migration[4.2][5.0]
  def change
    create_table :steps do |t|
      t.string :title
      t.string :description
      t.timestamps
    end
  end
end
