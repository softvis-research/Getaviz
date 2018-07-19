module BasicAuthHelper
  def login_as_admin
    user = 'admin'
    request.env['HTTP_AUTHORIZATION'] = ActionController::HttpAuthentication::Basic.encode_credentials(user,Rails.application.credentials.admin_password)
  end  
end
