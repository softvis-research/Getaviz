class CreateExperimentSurveys < ActiveRecord::Migration[4.2]
  def change
    create_table :experiment_surveys do |t|
      t.references :survey, index: true
      t.references :scene, index: true

      t.timestamps
    end
  end
end
