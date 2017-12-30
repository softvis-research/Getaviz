class REmoveAdditionalFiles < ActiveRecord::Migration[5.0]
  def change
    drop_table :additional_files
  end
end
