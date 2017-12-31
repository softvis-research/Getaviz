class AddIdHashToScene < ActiveRecord::Migration[4.2]
  def change
    add_column :scenes, :id_hash, :string
  end
end
