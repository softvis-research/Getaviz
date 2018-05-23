class AdministrationController < ApplicationController

  def admin_mode_on
    request.session_options[:skip] = false
    session[:admin] = true
    redirect_back(fallback_location: root_path)
  end

  def admin_mode_off
    request.session_options[:skip] = false
    session.delete(:admin)
    redirect_back(fallback_location: root_path)
  end

end
