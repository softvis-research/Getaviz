FROM ruby:2.4.5
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        rails rake build-essential nodejs default-libmysqlclient-dev mysql-client\
    && rm -rf /var/lib/apt/lists/* && mkdir -p /usr/src/app
WORkDIR /usr/src/app/
COPY . .
RUN gem update --system
RUN gem install bundler && bundle install --jobs 20 --retry 5 # might be better?
RUN gem install bundler
LABEL maintainer="david.baum@uni-leipzig.de" \
      version="1.0"
EXPOSE 8081
#CMD ["bin", "docker_start"]
