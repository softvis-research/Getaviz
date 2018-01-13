class AddUseAniFraToTest < ActiveRecord::Migration[4.2]
  def change
    add_column :tests, :use_framework, :boolean
  end
end
