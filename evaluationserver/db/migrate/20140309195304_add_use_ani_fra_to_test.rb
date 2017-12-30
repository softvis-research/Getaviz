class AddUseAniFraToTest < ActiveRecord::Migration
  def change
    add_column :tests, :use_framework, :boolean
  end
end
