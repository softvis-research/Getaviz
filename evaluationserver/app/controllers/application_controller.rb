class ApplicationController < ActionController::Base
  # Prevent CSRF attacks by raising an exception.
  # For APIs, you may want to use :null_session instead.
  protect_from_forgery with: :exception

  before_filter :set_cache_headers

  CREDENTIALS = {
    :name => 'admin',
    :password => ENV['ADMIN_PASSWORD']

  }

  before_action :choose_layout
  before_action :http_basic_authenticate

  before_action :set_locale
 
  def set_locale
    I18n.locale = params[:locale] || I18n.default_locale
  end

  def default_url_options(options={})
    { locale: I18n.locale }
  end


  private 


  def choose_layout
    if session[:admin]
      self.class.layout 'administration'
    else
      self.class.layout 'application'
    end
  end
  
  def http_basic_authenticate
    authenticate_or_request_with_http_basic do |name, password|
      name == CREDENTIALS[:name] && password == CREDENTIALS[:password]
    end
  end


  def set_cache_headers
    response.headers["Cache-Control"] = "no-cache, no-store"
    response.headers["Pragma"] = "no-cache"
    response.headers["Expires"] = "Fri, 01 Jan 1990 00:00:00 GMT"
  end

end
