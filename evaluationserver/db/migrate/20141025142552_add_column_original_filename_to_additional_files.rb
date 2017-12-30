class AddColumnOriginalFilenameToAdditionalFiles < ActiveRecord::Migration
  def change
    add_column :additional_files, :original_name, :string
  end
end
