class CreateAdditionalFiles < ActiveRecord::Migration[4.2]
  def change
    create_table :additional_files do |t|
      t.attachment :file
      t.references :scene, index: true
      t.integer :type
      t.timestamps
    end
  end
end
