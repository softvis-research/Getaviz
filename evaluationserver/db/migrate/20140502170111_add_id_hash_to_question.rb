class AddIdHashToQuestion < ActiveRecord::Migration
  def change
    add_column :questions, :id_hash, :string
  end
end
