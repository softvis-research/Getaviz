class AddColumntypeToAnswers < ActiveRecord::Migration[4.2][5.0]
  def change
    add_column :answers, :type, :string
  end
end
