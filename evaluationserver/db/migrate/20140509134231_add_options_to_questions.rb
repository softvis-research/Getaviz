class AddOptionsToQuestions < ActiveRecord::Migration[4.2]
  def change
    add_column :questions, :options, :text
  end
end
