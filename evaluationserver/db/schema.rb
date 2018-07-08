# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# Note that this schema.rb definition is the authoritative source for your
# database schema. If you need to create the application database on another
# system, you should be using db:schema:load, not running all the migrations
# from scratch. The latter is a flawed and unsustainable approach (the more migrations
# you'll amass, the slower it'll run and the greater likelihood for issues).
#
# It's strongly recommended that you check this file into your version control system.

ActiveRecord::Schema.define(version: 20161002202724) do

  create_table "answer_possibility_collections", id: :integer, force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=utf8" do |t|
    t.integer "question_id"
    t.string "answer_type"
    t.integer "timeout"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.integer "max_mistakes"
    t.index ["question_id"], name: "index_answer_possibility_collections_on_question_id"
  end

  create_table "answers", id: :integer, force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=utf8" do |t|
    t.integer "answer_possibility_collection_id"
    t.string "answer_text"
    t.string "placeholder"
    t.float "min", limit: 24
    t.float "max", limit: 24
    t.float "step", limit: 24
    t.string "default"
    t.string "correct_answer_string"
    t.float "correct_answer_value_min", limit: 24
    t.float "correct_answer_value_max", limit: 24
    t.boolean "is_correct_choice"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.string "type"
    t.index ["answer_possibility_collection_id"], name: "index_answers_on_answer_possibility_collection_id"
  end

  create_table "experiment_steps", id: :integer, force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=utf8" do |t|
    t.integer "position"
    t.integer "experiment_id"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.string "id_hash"
    t.integer "step_id"
    t.boolean "is_inner_step", default: false
    t.index ["experiment_id"], name: "index_experiment_steps_on_experiment_id"
    t.index ["step_id"], name: "index_experiment_steps_on_step_id"
  end

  create_table "experiment_surveys", id: :integer, force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=utf8" do |t|
    t.integer "survey_id"
    t.integer "scene_id"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.index ["scene_id"], name: "index_experiment_surveys_on_scene_id"
    t.index ["survey_id"], name: "index_experiment_surveys_on_survey_id"
  end

  create_table "experiments", id: :integer, force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=utf8" do |t|
    t.string "title"
    t.text "description"
    t.string "id_hash"
    t.integer "greeting_id"
    t.integer "farewell_id"
    t.integer "post_test_id"
    t.integer "pre_test_id"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.integer "first_step_id"
    t.index ["farewell_id"], name: "index_experiments_on_farewell_id"
    t.index ["first_step_id"], name: "index_experiments_on_first_step_id"
    t.index ["greeting_id"], name: "index_experiments_on_greeting_id"
    t.index ["post_test_id"], name: "index_experiments_on_post_test_id"
    t.index ["pre_test_id"], name: "index_experiments_on_pre_test_id"
  end

  create_table "participant_experiment_step_answers", id: :integer, force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=utf8" do |t|
    t.text "answer"
    t.integer "question_id"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.integer "time_needed_in_ms"
    t.integer "mistakes"
    t.integer "helps"
    t.boolean "answered_correctly"
    t.integer "participant_experiment_step_id"
    t.text "answers"
    t.index ["question_id"], name: "index_participant_experiment_step_answers_on_question_id"
  end

  create_table "participant_experiment_step_scenes", id: :integer, force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=utf8" do |t|
    t.integer "scene_id"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.integer "participant_experiment_step_id"
    t.text "log_hash"
    t.integer "number_of_clicks"
    t.integer "time_of_mouse_down"
    t.integer "mouse_wheel_interaction", default: 0
    t.integer "number_of_resets"
    t.index ["scene_id"], name: "index_participant_experiment_step_scenes_on_scene_id"
  end

  create_table "participant_experiment_steps", id: :integer, force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=utf8" do |t|
    t.integer "experiment_step_id"
    t.integer "participant_id"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.string "id_hash"
    t.datetime "started"
    t.datetime "done"
    t.index ["experiment_step_id"], name: "index_participant_experiment_steps_on_experiment_step_id"
    t.index ["participant_id"], name: "index_participant_experiment_steps_on_participant_id"
  end

  create_table "participants", id: :integer, force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=utf8" do |t|
    t.string "id_hash"
    t.integer "experiment_id"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "question_options", id: :integer, force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=utf8" do |t|
    t.string "answer"
    t.integer "question_id"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.index ["question_id"], name: "index_question_options_on_question_id"
  end

  create_table "question_types", id: :integer, force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=utf8" do |t|
    t.string "name"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "questionaire_questions", id: :integer, force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=utf8" do |t|
    t.integer "questionaire_id"
    t.integer "question_id"
    t.integer "position"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["question_id"], name: "index_questionaire_questions_on_question_id"
    t.index ["questionaire_id"], name: "index_questionaire_questions_on_questionaire_id"
  end

  create_table "questions", id: :integer, force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=utf8" do |t|
    t.text "question_text"
    t.integer "question_type_id"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.string "id_hash"
    t.text "options"
    t.index ["question_type_id"], name: "index_questions_on_question_type_id"
  end

  create_table "scene_test_scenes", id: :integer, force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=utf8" do |t|
    t.integer "scene_test_id"
    t.integer "scene_id"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.index ["scene_id"], name: "index_scene_test_scenes_on_scene_id"
    t.index ["scene_test_id"], name: "index_scene_test_scenes_on_scene_test_id"
  end

  create_table "scene_tests", id: :integer, force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=utf8" do |t|
    t.string "title"
    t.text "description"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.integer "test_id"
    t.integer "assigner_id"
    t.index ["assigner_id"], name: "index_scene_tests_on_assigner_id"
    t.index ["test_id"], name: "index_scene_tests_on_test_id"
  end

  create_table "scenes", id: :integer, force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=utf8" do |t|
    t.string "name"
    t.string "description"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.string "id_hash"
    t.string "url"
    t.text "remarks"
    t.text "mapping"
  end

  create_table "step_scenes", id: :integer, force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=utf8" do |t|
    t.integer "step_id"
    t.integer "scene_id"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["scene_id"], name: "index_step_scenes_on_scene_id"
    t.index ["step_id"], name: "index_step_scenes_on_step_id"
  end

  create_table "step_textual_elements", id: :integer, force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=utf8" do |t|
    t.integer "step_id"
    t.integer "textual_element_id"
    t.integer "position"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.index ["step_id"], name: "index_step_textual_elements_on_step_id"
    t.index ["textual_element_id"], name: "index_step_textual_elements_on_textual_element_id"
  end

  create_table "steps", id: :integer, force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=utf8" do |t|
    t.string "title"
    t.string "description"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.integer "parent_step_id"
    t.string "type", default: "Step"
    t.integer "position"
    t.boolean "use_scene_from_parent_group"
  end

  create_table "test_questions", id: :integer, force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=utf8" do |t|
    t.integer "test_id"
    t.integer "question_id"
    t.integer "position"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.index ["question_id"], name: "index_test_questions_on_question_id"
    t.index ["test_id"], name: "index_test_questions_on_test_id"
  end

  create_table "tests", id: :integer, force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=utf8" do |t|
    t.string "title"
    t.string "description"
    t.datetime "created_at"
    t.datetime "updated_at"
  end

  create_table "texts", id: :integer, force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=utf8" do |t|
    t.text "html"
    t.datetime "created_at"
    t.datetime "updated_at"
    t.string "title"
  end

  create_table "textual_elements", id: :integer, force: :cascade, options: "ENGINE=InnoDB DEFAULT CHARSET=utf8" do |t|
    t.string "title"
    t.string "description"
    t.text "html"
    t.integer "questionaire_type_id"
    t.integer "timeout"
    t.datetime "created_at", null: false
    t.datetime "updated_at", null: false
    t.string "type"
  end

  add_foreign_key "answer_possibility_collections", "questions"
  add_foreign_key "answers", "answer_possibility_collections"
  add_foreign_key "experiment_steps", "steps"
end
