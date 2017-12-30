class CreateGreetings < ActiveRecord::Migration
  def change
    create_table :greetings do |t|
      t.text :text

      t.timestamps
    end
  end
end
