class AddOptionsToQuestions < ActiveRecord::Migration
  def change
    add_column :questions, :options, :text
  end
end
