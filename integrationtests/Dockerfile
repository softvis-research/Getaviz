FROM maven:3.6.0-jdk-8-alpine
RUN mkdir -p /tmp/integrationtests/
COPY ./org.getaviz.tests/ /tmp/integrationtests/
COPY ./bin/wait-for-it.sh /bin/wait-for-it.sh
WORKDIR /tmp/integrationtests/
# ENTRYPOINT sleep 30 && mvn install
