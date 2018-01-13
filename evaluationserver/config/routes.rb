Rails.application.routes.draw do

  get 'scenes/(:id)/full/index' => 'scenes#full', :as => :full_scene

  get 'scenes/(:scene_id)/detail/*file' => 'additional_files#get_file'
  get 'scenes/(:scene_id)/full/*file' => 'additional_files#get_file'


  resources :scenes do |member|
    member do 
      get 'detail'
      
    end
  end

  resources :texts

  resources :experiment_tests

  resources :steps

  resources :questions

  resources :questionaires do |member|
    member do
      get 'conduct'
    end
  end

  resources :participants

  resources :experiments do |member|
    member do
      get 'results'
      
    end
  end


  get 'administration/on' => 'administration#admin_mode_on', as: :admin_mode_on
  get 'administration/off' => 'administration#admin_mode_off', as: :admin_mode_off



  ### experiment starten
  get 'start/:experiment_hash' => 'experiments#start', as: :start

  
  ### id_hash ist der Hash des ParticipantExperimentSteps
  get 'conduct/:id_hash' => 'experiments#conduct', as: :conduct
  patch 'conduct/:id_hash' => 'experiments#finish_step', as: :finish_step
  get 'experiment/already_done/:id_hash' => "experiments#already_done", as: :already_done
  post 'experiment/check_answer/:id_hash/:questionaire_id/:answer_collection_id' => "experiments#check_answer", as: :check_answer
  
  
  get 'experiment/import' => "experiments#import", as: :import_experiment
  post 'experiment/upload' => "experiments#upload", as: :upload_experiment

  get 'experiment/csv/:id' => "experiments#csv", as: :experiment_result_csv
  get 'experiment/csv_interaction/:id' => "experiments#csv_interaction", as: :experiment_result_interaction_csv
  
  root 'experiments#index'

end
