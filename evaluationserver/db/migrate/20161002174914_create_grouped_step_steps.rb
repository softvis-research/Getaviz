class CreateGroupedStepSteps < ActiveRecord::Migration[5.0]
  def change
    add_column :steps, :parent_step_id, :integer
    add_column :steps, :type, :string, :default => 'Step'
    add_column :steps, :position, :integer
    
  end
end
