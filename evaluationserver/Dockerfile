FROM getaviz/evaluationserver-base:1.2.0
WORkDIR /usr/src/app/
COPY . .
RUN bundle install --jobs 20 --retry 5 && gem install bundler
LABEL maintainer="david.baum@uni-leipzig.de" \
      version="1.0"
EXPOSE 8081
