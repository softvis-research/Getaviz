module Features
  module SessionHelpers
    def authenticate_as_admin
      admin_user = "admin"
      page.driver.browser.authorize(admin_user,ENV['ADMIN_PASSWORD'])
    end


    def t(*args)
      I18n.translate!(*args)
    end


    
  end
end
