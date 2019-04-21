FROM maven:3.6.0-jdk-8-alpine
RUN mkdir -p /tmp/integrationtests/
COPY . /tmp/integrationtests/
WORKDIR /tmp/integrationtests/
ENTRYPOINT sleep 30 && mvn install
