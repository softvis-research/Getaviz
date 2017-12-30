class AddColumntypeToAnswers < ActiveRecord::Migration[5.0]
  def change
    add_column :answers, :type, :string
  end
end
