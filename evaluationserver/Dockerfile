FROM ruby:2.4.1
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        rails rake build-essential libmysqlclient-dev mysql-client\
    && rm -rf /var/lib/apt/lists/*
RUN mkdir -p /usr/src/app
WORkDIR /usr/src/app/
COPY ./Gemfile /usr/src/app
COPY ./Gemfile.lock /usr/src/app
# RUN gem install bundler && bundle install --jobs 20 --retry 5 # might be better?
RUN gem install bundler
RUN bundle install
#RUN bundle exec rake db:create
#RUN bundle exec rake db:migrate
LABEL maintainer="david.baum@uni-leipzig.de" \
      version="1.0"

CMD ["bundle", "exec", "rails", "server", "-b", "0.0.0.0", "-p", "8081"]
