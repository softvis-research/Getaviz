class ScenesController < ApplicationController
  before_action :set_scene, only: [:show, :edit, :update, :destroy, :detail, :full]

  skip_before_action :http_basic_authenticate, :only => [:full]

  # GET /scenes
  # GET /scenes.json
  def index
    @scenes = Scene.all
  end

  # GET /scenes/1
  # GET /scenes/1.json
  def show
  end

  def full
    if @scene.use_url
      url = URI.parse(@scene.url)
      result = Net::HTTP.get_response(url)
      send_data result.body, :type => result.content_type, :disposition => 'inline'
    else
      render :layout => "none"
    end
  end


 

  def detail
    render :layout => "blank"
  end


  # GET /scenes/new
  def new
    @scene = Scene.new
  end

  # GET /scenes/1/edit
  def edit
  end

  # POST /scenes
  # POST /scenes.json
  def create
    @scene = Scene.new(scene_params)

    respond_to do |format|
      if @scene.save
        format.html { redirect_to @scene, notice: t(:successful_created, scope: :scenes) }
        format.json { render :show, status: :created, location: @scene }
      else
        format.html { render :new }
        format.json { render json: @scene.errors, status: :unprocessable_entity }
      end
    end
  end

  # PATCH/PUT /scenes/1
  # PATCH/PUT /scenes/1.json
  def update
    respond_to do |format|
      if @scene.update(scene_params)
        format.html { redirect_to @scene, notice: t(:successful_updated, scope: :scenes) }
        format.json { render :show, status: :ok, location: @scene }
      else
        format.html { render :edit }
        format.json { render json: @scene.errors, status: :unprocessable_entity }
      end
    end
  end

  # DELETE /scenes/1
  # DELETE /scenes/1.json
  def destroy
    @scene.destroy
    respond_to do |format|
      format.html { redirect_to scenes_url }
      format.json { head :no_content }
    end
  end

  


  private
    # Use callbacks to share common setup or constraints between actions.
    def set_scene
      @scene = Scene.find(params[:id])
    end

    # Never trust parameters from the scary internet, only allow the white list through.
    def scene_params
      params.require(:scene).permit(:name, :description, :scenefile, :use_url, :remarks, :mapping, :url)
    end
end
