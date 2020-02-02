Experiment.create!([
  {title: "Testexperiment", description: "simple experiment with different steps", id_hash: "8c75643b-7484-4316-9cbc-d0694b4a1710", greeting_id: nil, farewell_id: nil, post_test_id: nil, pre_test_id: nil, first_step_id: nil}
])

Step.create!([
  {title: "Welcome Step", description: "", parent_step_id: nil, type: "Step", position: 1, use_scene_from_parent_group: nil},
  {title: "Basic questionaire", description: "", parent_step_id: nil, type: "Step", position: 2, use_scene_from_parent_group: nil},
  {title: "Scene without questionaire", description: "", parent_step_id: nil, type: "Step", position: 3, use_scene_from_parent_group: nil},
  {title: "Scene with questionaire", description: "", parent_step_id: nil, type: "Step", position: 4, use_scene_from_parent_group: nil},
  {title: "Thank you", description: "", parent_step_id: nil, type: "Step", position: 5, use_scene_from_parent_group: nil}
])







ExperimentStep.create!([
  {position: 1, experiment_id: 1, id_hash: "33fb8f7f-c558-49a4-a5d4-fbe75d8284ab", step_id: 1, is_inner_step: false},
  {position: 2, experiment_id: 1, id_hash: "71ecfeca-9429-4541-8b36-ad9a55568a94", step_id: 2, is_inner_step: false},
  {position: 3, experiment_id: 1, id_hash: "0405be0e-e29a-4bf9-99dd-e0452c7ed327", step_id: 3, is_inner_step: false},
  {position: 4, experiment_id: 1, id_hash: "2f54ae00-8676-4581-9108-270f1bf86ddc", step_id: 4, is_inner_step: false},
  {position: 5, experiment_id: 1, id_hash: "6cb8d936-ab5d-4a23-8541-f86069b655c8", step_id: 5, is_inner_step: false}
])


TextualElement.create!([
  {title: "Welcome", description: nil, html: "<p>Welcome to the example experiment. You will be guided through 5 steps:</p>\r\n<p>1. this welcome</p>\r\n<p>2. a simple questionaire</p>\r\n<p>3. one of two scenes (the scenes are from http://home.uni-leipzig.de/svis/Showcases/)</p>\r\n<p>4. one of the two scenes again but with a questionaire integrated</p>\r\n<p>5. a final static text&nbsp;</p>", questionaire_type_id: nil, timeout: nil, type: "Text"},
  {title: "Basic questionaire", description: "", html: nil, questionaire_type_id: nil, timeout: nil, type: "Questionaire"},
  {title: "Scene questionaire", description: "", html: nil, questionaire_type_id: nil, timeout: nil, type: "Questionaire"},
  {title: "Thanks", description: nil, html: "<p>Thank you for testing the evaluation server</p>", questionaire_type_id: nil, timeout: nil, type: "Text"}
])

Question.create!([
  {question_text: "How old are you?", question_type_id: nil, id_hash: "a7d7ea82-9e84-45aa-9b24-ddbf8509dd1e", options: nil},
  {question_text: "What is your favorite color?", question_type_id: nil, id_hash: "41fe8121-605f-48bd-a576-c93fa7b1d44c", options: nil},
  {question_text: "How many projects are visualized in the shown scene?", question_type_id: nil, id_hash: "32fdca34-412f-4c18-8c15-5baa9a8ac015", options: nil}
])

QuestionaireQuestion.create!([
  {questionaire_id: 2, question_id: 1, position: 1},
  {questionaire_id: 2, question_id: 2, position: 1},
  {questionaire_id: 3, question_id: 3, position: 1}
])




AnswerPossibilityCollection.create!([
  {question_id: 1, answer_type: "slider", timeout: nil, max_mistakes: nil},
  {question_id: 2, answer_type: "freetext", timeout: nil, max_mistakes: nil},
  {question_id: 3, answer_type: "choice", timeout: nil, max_mistakes: 2},
  {question_id: 3, answer_type: "slider", timeout: nil, max_mistakes: 1}
                                    ])



Scene.create!([
  {name: "Freemind City", description: "Example scene showing a city metaphor visualizing freemind", id_hash: "561194dd-ef10-4057-816a-a0ca450ebf74", url: "https://home.uni-leipzig.de/svis/getaviz/index.php?setup=web/City%20freemind&model=City%20floor%20freemind", remarks: "<p>Here can remarks for using the visualization be placed</p>", mapping: "<p>Here is the place for describing how the elements of the software are visualized</p>"},
  {name: "Freemind RD", description: "Scene showing the project Freemind visualized with a recursive disk metaphor ", id_hash: "b9bde7c5-97a0-4824-b2b4-20419688e7c0", url: "https://home.uni-leipzig.de/svis/getaviz/index.php?setup=web/RD%20freemind&model=RD%20freemind", remarks: "<p>This is the place for remarks like usage information</p>", mapping: "<p>This is the place for information regarding how the elements of the software are visualized&nbsp;</p>"}
])
StepScene.create!([
  {step_id: 3, scene_id: 1},
  {step_id: 3, scene_id: 2},
  {step_id: 4, scene_id: 1},
  {step_id: 4, scene_id: 2}
])
StepTextualElement.create!([
  {step_id: 1, textual_element_id: 1, position: nil},
  {step_id: 2, textual_element_id: 2, position: nil},
  {step_id: 4, textual_element_id: 3, position: nil},
  {step_id: 5, textual_element_id: 4, position: nil}
])
Answer::Choice.create!([
  {answer_possibility_collection_id: 3, answer_text: "1", placeholder: "", min: nil, max: nil, step: nil, default: "", correct_answer_string: "", correct_answer_value_min: nil, correct_answer_value_max: nil, is_correct_choice: true, type: "Answer::Choice"},
  {answer_possibility_collection_id: 3, answer_text: "10", placeholder: "", min: nil, max: nil, step: nil, default: "", correct_answer_string: "", correct_answer_value_min: nil, correct_answer_value_max: nil, is_correct_choice: false, type: "Answer::Choice"},
  {answer_possibility_collection_id: 3, answer_text: "40", placeholder: "", min: nil, max: nil, step: nil, default: "", correct_answer_string: "", correct_answer_value_min: nil, correct_answer_value_max: nil, is_correct_choice: false, type: "Answer::Choice"}
])
Answer::FreeText.create!([
  {answer_possibility_collection_id: 2, answer_text: "", placeholder: "", min: nil, max: nil, step: nil, default: "", correct_answer_string: "", correct_answer_value_min: nil, correct_answer_value_max: nil, is_correct_choice: false, type: "Answer::FreeText"}
])
Answer::Slider.create!([
  {answer_possibility_collection_id: 1, answer_text: "", placeholder: "", min: 18.0, max: 100.0, step: 1.0, default: "18", correct_answer_string: "", correct_answer_value_min: nil, correct_answer_value_max: nil, is_correct_choice: false, type: "Answer::Slider"},
  {answer_possibility_collection_id: 4, answer_text: "", placeholder: "", min: 1.0, max: 1000.0, step: 1.0, default: "2", correct_answer_string: "", correct_answer_value_min: 1.0, correct_answer_value_max: 1.0, is_correct_choice: false, type: "Answer::Slider"}
])
Questionaire.create!([
  {title: "Basic questionaire", description: "", html: nil, questionaire_type_id: nil, timeout: nil, type: "Questionaire"},
  {title: "Scene questionaire", description: "", html: nil, questionaire_type_id: nil, timeout: nil, type: "Questionaire"}
])
Text.create!([
  {title: "Welcome", description: nil, html: "<p>Welcome to the example experiment. You will be guided through 5 steps:</p>\r\n<p>1. this welcome</p>\r\n<p>2. a simple questionaire</p>\r\n<p>3. one of two scenes (the scenes are from http://home.uni-leipzig.de/svis/Showcases/)</p>\r\n<p>4. one of the two scenes again but with a questionaire integrated</p>\r\n<p>5. a final static text&nbsp;</p>", questionaire_type_id: nil, timeout: nil, type: "Text"},
  {title: "Thanks", description: nil, html: "<p>Thank you for testing the evaluation server</p>", questionaire_type_id: nil, timeout: nil, type: "Text"}
])
