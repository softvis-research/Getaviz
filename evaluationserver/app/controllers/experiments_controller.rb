# -*- coding: utf-8 -*-
class ExperimentsController < ApplicationController

  skip_before_action :http_basic_authenticate, :only => [:index, :start, :conduct, :finish_step, :check_answer, :already_done]

  before_action :set_experiment, only: [:show, :edit, :update, :destroy, :results, :csv, :csv_interaction]
  before_action :set_experiment_by_experiment_hash, only: [:start]
  before_action :set_participant_experiment_step, only: [:conduct, :finish_step, :already_done, :check_answer]
  before_action :set_questionaire, only: [:check_answer]

  # GET /experiments
  # GET /experiments.json
  def index
    @experiments = Experiment.all
  end

  # GET /experiments/1
  # GET /experiments/1.json
  def show
  end

  # GET /experiments/new
  def new
    @experiment = Experiment.new
  end

  # GET /experiments/1/edit
  def edit
  end


  # POST /experiments
  # POST /experiments.json
  def create
    @experiment = Experiment.new(experiment_params)

    respond_to do |format|
      if @experiment.save
        format.html { redirect_to @experiment, notice: t(:successful_created, scope: :experiments)  }
        format.json { render action: 'show', status: :created, location: @experiment }
      else
        format.html { render action: 'new' }
        format.json { render json: @experiment.errors, status: :unprocessable_entity }
      end
    end
  end

  # PATCH/PUT /experiments/1
  # PATCH/PUT /experiments/1.json
  def update
    respond_to do |format|
      @experiment.steps.clear
      if @experiment.update(experiment_params)
        format.html { redirect_to @experiment, notice: t(:successful_updated, scope: :experiments) }
        format.json { head :no_content }
      else
        format.html { render action: 'edit' }
        format.json { render json: @experiment.errors, status: :unprocessable_entity }
      end
    end
  end

  # DELETE /experiments/1
  # DELETE /experiments/1.json
  def destroy
    @experiment.destroy
    respond_to do |format|
      format.html { redirect_to experiments_url }
      format.json { head :no_content }
    end
  end

  def start
    session[:admin] = false
    participant = Participant.create()
    @experiment.participants << participant
    @experiment_step = @experiment.experiment_steps.first
    @participant_experiment_step = @experiment_step.participant_experiment_steps.create(:participant => participant)
    redirect_to conduct_path({:id_hash => @participant_experiment_step.id_hash})
  end

  def conduct
    if @participant_experiment_step.nil?
      render :layout => 'blank', :status => :error, :text => 'No such step'
      return
    end

    
    unless @participant_experiment_step.experiment_step.step.scenes.empty?
      
      
      
      @participant_experiment_step.scenes.clear
      scene = @participant_experiment_step.experiment_step.step.get_random_scene(@participant_experiment_step.experiment_step)
      @participant_experiment_step.scenes << scene

      puts "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa"
      puts "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa"
      puts "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa"
      puts scene.name
      puts "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa"
      puts "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa"
      puts "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa"

      
    end
    
    if @participant_experiment_step.started # 
      ### wurde von diesem Teilnehmer schon durchgeführt
      redirect_to already_done_path(@participant_experiment_step.id_hash)
      return
    end

    @participant_experiment_step.update_attributes(started: Time.now)
    @participant_experiment_step.save!
    if @participant_experiment_step.experiment_step.step.is_a?(GroupedStep)
      ### abtauchen in gruppierten Schritt
      
      @participant_experiment_step = @participant_experiment_step.get_first_inner_step
      redirect_to conduct_path({:id_hash => @participant_experiment_step.id_hash})
    else
      render :layout => 'blank'
    end
    
    
  end

  def finish_step
    save_scene_interaction
    @participant_experiment_step.update_attributes(done: Time.now)
    @participant_experiment_step = @participant_experiment_step.get_next_step
    if @participant_experiment_step
      redirect_to conduct_path({:id_hash => @participant_experiment_step.id_hash})
    else
      redirect_to :root
    end
  end

  def results
    @experiment_results = @experiment.results
  end

  def import
    
  end

  def already_done
    ### TODO: Möglichkeit zum Fortsetzen schaffen
    render :layout => 'blank'
  end
  
  def check_answer
    save_scene_interaction
    @apc = AnswerPossibilityCollection.find(params[:answer_collection_id])
    apc_index = @apc.question.answer_possibility_collections.to_a.index(@apc)
    given_answer = params[:answers][@apc.id.to_s][:answer]

    
    
    @participant_experiment_step_answer = @participant_experiment_step.participant_experiment_step_answers.where(:question => @apc.question).first_or_create
    @participant_experiment_step_answer.answer = given_answer
    @participant_experiment_step_answer.time_needed_in_ms = params[:answers][@apc.id.to_s][:time].to_i
    @participant_experiment_step_answer.save!

    if @apc.has_correct_answer?
      ### check answer
      is_correct = @apc.is_correct_answered_with?(given_answer)

      
      
      @participant_experiment_step_answer.answered_correctly = is_correct
      @participant_experiment_step_answer.mistakes = 0 unless @participant_experiment_step_answer.mistakes
      @participant_experiment_step_answer.helps = 0 unless @participant_experiment_step_answer.helps
      unless is_correct
        @participant_experiment_step_answer.mistakes = @participant_experiment_step_answer.mistakes + 1 
        @participant_experiment_step_answer.helps = @participant_experiment_step_answer.helps + 1
      
      end
      @participant_experiment_step_answer.save!
      
      ## is max mistakes reached?
      
      max_mistakes = (@apc.question.answer_possibility_collections.to_a[0..apc_index].collect(&:max_mistakes).inject(0){|sum,x| sum + [x.to_i,1].max })

      if @participant_experiment_step_answer.mistakes >= max_mistakes
        ## max mistakes reached
        puts @participant_experiment_step_answer.mistakes
        @next_apc = @apc.question.answer_possibility_collections[apc_index + 1]
      else
        @next_apc = @apc
      end

      if is_correct
        @answered_partial = 'correct_answer'
        question_index = @questionaire.questions.to_a.index(@apc.question)
        @question = @questionaire.questions.to_a[question_index + 1]
        @participant_experiment_step_answer = nil
        
        if @question
          render "next_question"
        else
          render "finish_step"
        end
      else
        if @next_apc
          render "next_answer"
        else
          question_index = @questionaire.questions.to_a.index(@apc.question)
          @question = @questionaire.questions.to_a[question_index + 1]
          @participant_experiment_step_answer = nil
          @answered_partial = 'incorrect_answer'
          if @question
            render "next_question"
          else
            render "finish_step"
          end
        end
      end
    else
      question_index = @questionaire.questions.to_a.index(@apc.question)
      @question = @questionaire.questions.to_a[question_index + 1]
      @participant_experiment_step_answer.answered_correctly = is_correct
      @participant_experiment_step_answer.save!
      @participant_experiment_step_answer = nil
      @answered_partial = 'answer_given'
      if @question
        
        render "next_question"
      else
        render "finish_step"
      end
    end
  end

  def csv
    
    csv_string = CSV.generate do |csv|
      csv << ["question", "participant", "scene", "step", "mistakes", "calculated_mistakes", "time_needed_in_ms", "answer", "correct_answer", "answered_correctly"]
      @experiment.all_answers.each{|answer|
        csv << [answer[:question], answer[:participant], answer[:scene], answer[:step], answer[:mistakes], answer[:calculated_mistakes], answer[:time_needed_in_ms], answer[:answer], answer[:correct_answer], answer[:answered_correctly]]
      }
    end
    send_data csv_string
  end

  def csv_interaction
    csv_string = CSV.generate do |csv|
      csv << ["participant", "scene", "number_of_clicks", "time_of_mouse_down", "mouse_wheel_interaction", "number_of_resets"]
      ParticipantExperimentStepScene.includes(:participant_experiment_step =>  {:experiment_step => :experiment}  ).where({:experiment_steps => {:experiment_id => @experiment.id}}).each{|participant_experiment_step_scene|
        next if participant_experiment_step_scene.number_of_clicks.to_i == 0 and
          participant_experiment_step_scene.time_of_mouse_down.to_i == 0 and
          participant_experiment_step_scene.mouse_wheel_interaction.to_i == 0 and
          participant_experiment_step_scene.number_of_resets.to_i == 0 
        pes = participant_experiment_step_scene.participant_experiment_step
        csv << [pes.participant_id, participant_experiment_step_scene.scene.name, participant_experiment_step_scene.number_of_clicks.to_i, participant_experiment_step_scene.time_of_mouse_down.to_i, participant_experiment_step_scene.mouse_wheel_interaction.to_i, participant_experiment_step_scene.number_of_resets.to_i]
      }
    end
    send_data csv_string
  end
  
  private
    # Use callbacks to share common setup or constraints between actions.
    def set_experiment
      @experiment = Experiment.find(params[:id])
    end

    def set_experiment_by_experiment_hash
      @experiment = Experiment.where(id_hash: params[:experiment_hash]).first
    end

    def set_participant_experiment_step
      @participant_experiment_step = ParticipantExperimentStep.where(id_hash: params[:id_hash]).first
    end

    def set_questionaire
      @questionaire = Questionaire.find(params[:questionaire_id])
    end

    # Never trust parameters from the scary internet, only allow the white list through.
    def experiment_params
      params.require(:experiment).permit(:title, :description, :step_ids => [])
    end

    def scene_interaction_params
      {:scene_mousedown_time => params[:scene_mousedown_time], :scene_click_counter => params[:scene_click_counter], :scene_mousewheel_counter => params[:scene_mousewheel_counter], :scene_resets => params[:scene_resets]}
    end

    def save_scene_interaction
      
      if @participant_experiment_step and (@participant_experiment_step.participant_experiment_step_scenes.length > 0 or  (@participant_experiment_step.get_parent_participant_experiment_step and @participant_experiment_step.get_parent_participant_experiment_step.participant_experiment_step_scenes.length > 0))
        the_participant_experiment_step_scenes = @participant_experiment_step.participant_experiment_step_scenes if @participant_experiment_step.participant_experiment_step_scenes.length > 0
        the_participant_experiment_step_scenes = @participant_experiment_step.get_parent_participant_experiment_step.participant_experiment_step_scenes if (@participant_experiment_step.get_parent_participant_experiment_step and @participant_experiment_step.get_parent_participant_experiment_step.participant_experiment_step_scenes.length > 0)
        
        if scene_interaction_params
          scene_mousedown_time = scene_interaction_params[:scene_mousedown_time].to_i
          scene_click_counter = scene_interaction_params[:scene_click_counter].to_i
          scene_mousewheel_counter = scene_interaction_params[:scene_mousewheel_counter].to_i
          scene_resets = scene_interaction_params[:scene_resets].to_i
          the_participant_experiment_step_scenes.each{|pess|
            if pess.time_of_mouse_down.to_i < scene_mousedown_time
              pess.time_of_mouse_down = scene_mousedown_time
            end
            if pess.number_of_clicks.to_i < scene_click_counter
              pess.number_of_clicks = scene_click_counter
            end
            if pess.mouse_wheel_interaction.to_i < scene_mousewheel_counter
              pess.mouse_wheel_interaction = scene_mousewheel_counter
            end
            if pess.number_of_resets.to_i < scene_resets
              pess.number_of_resets = scene_resets
            end
            pess.save!
          }
        end
      end
    end
end
