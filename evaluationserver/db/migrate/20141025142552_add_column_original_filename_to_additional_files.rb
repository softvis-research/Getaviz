class AddColumnOriginalFilenameToAdditionalFiles < ActiveRecord::Migration[4.2]
  def change
    add_column :additional_files, :original_name, :string
  end
end
