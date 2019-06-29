FROM getaviz/evaluationserver-base:development
WORkDIR /usr/src/app/
COPY . .
RUN gem update --system && gem install bundler && bundle install --jobs 20 --retry 5 && gem install bundler
LABEL maintainer="david.baum@uni-leipzig.de" \
      version="1.0"
EXPOSE 8081
