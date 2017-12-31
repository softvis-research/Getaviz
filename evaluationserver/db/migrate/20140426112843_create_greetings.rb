class CreateGreetings < ActiveRecord::Migration[4.2]
  def change
    create_table :greetings do |t|
      t.text :text

      t.timestamps
    end
  end
end
