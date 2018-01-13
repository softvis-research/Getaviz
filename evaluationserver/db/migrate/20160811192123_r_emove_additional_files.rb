class REmoveAdditionalFiles < ActiveRecord::Migration[4.2][5.0]
  def change
    drop_table :additional_files
  end
end
