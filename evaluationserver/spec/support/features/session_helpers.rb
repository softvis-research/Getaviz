module Features
  module SessionHelpers
    def authenticate_as_admin
      visit root_path
      click_link(t('layout.admin_on'))
      admin_user = "admin"
      page.driver.browser.basic_authorize(admin_user,Rails.application.credentials.admin_password)
    end


    def t(*args)
      I18n.translate!(*args)
    end


    
  end
end
