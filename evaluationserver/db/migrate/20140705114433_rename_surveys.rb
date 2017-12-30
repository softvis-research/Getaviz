class RenameSurveys < ActiveRecord::Migration
  def self.up
    if ActiveRecord::Base.connection.table_exists?('tests')
      drop_table :tests
    end
    if ActiveRecord::Base.connection.table_exists?('experiment_pre_tests')
      drop_table :experiment_pre_tests
    end
    if ActiveRecord::Base.connection.table_exists?('greetings')
      drop_table :greetings
    end
    if ActiveRecord::Base.connection.table_exists?('farewells')
      drop_table :farewells
    end

    rename_table :surveys, :tests
    if ActiveRecord::Base.connection.table_exists?('survey_questions')
      drop_table :survey_questions
    end
    if ActiveRecord::Base.connection.table_exists?('scenes')
      rename_table :scenes, :scene_tests
    else
      create_table :scene_tests do |t|
        t.string :title
        t.text :description
        t.attachment :scenefile
        
        t.timestamps
      end
    end
  end

  def self.down
    rename_table :scene_tests, :scenes
    rename_table :tests, :surveys
    rename_table :test_questions, :survey_questions
  end

end
