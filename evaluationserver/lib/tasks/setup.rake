namespace :setup do
  desc "Setup Database with an example experiment"
  task example: :environment do
    max_retries = 5
    begin
      Rake::Task["db:migrate"].execute
    rescue StandardError => e
      max_retries -= 1
      sleep 5
      if max_retries >= 0
        puts "db:migrate failed -> retry"
        retry
      else
        puts "db:migrate failed -> max retries reached"
      end
    end
    Rake::Task["db:seed"].invoke
  end

end
