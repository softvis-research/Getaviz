class AddIdHashToScene < ActiveRecord::Migration
  def change
    add_column :scenes, :id_hash, :string
  end
end
