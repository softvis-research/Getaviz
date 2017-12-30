class AdministrationController < ApplicationController

  def admin_mode_on
    session[:admin] = true
    redirect_to :back
  end

  def admin_mode_off
    session.delete(:admin)
    redirect_to :back
  end

end
