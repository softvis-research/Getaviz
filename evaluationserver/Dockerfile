FROM getaviz/evaluationserver-base:1.2.1
WORkDIR /usr/src/app/
COPY Gemfile .
COPY Gemfile.lock .
RUN bundle install --jobs 20 --retry 5 && gem install bundler
COPY . .
LABEL maintainer="david.baum@uni-leipzig.de" \
      version="1.0"
EXPOSE 8081
