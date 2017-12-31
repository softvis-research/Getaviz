class AdministrationController < ApplicationController

  def admin_mode_on
    session[:admin] = true
    redirect_back(fallback_location: root_path)
  end

  def admin_mode_off
    session.delete(:admin)
    redirect_back(fallback_location: root_path)
  end

end
