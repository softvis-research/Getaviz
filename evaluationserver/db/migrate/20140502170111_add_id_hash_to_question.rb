class AddIdHashToQuestion < ActiveRecord::Migration[4.2]
  def change
    add_column :questions, :id_hash, :string
  end
end
