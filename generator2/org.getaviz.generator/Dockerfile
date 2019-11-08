FROM maven:3.6.0-jdk-8-alpine AS MAVEN_TOOL_CHAIN
RUN mkdir -p /tmp/generator2/
COPY . /tmp/generator2/
WORKDIR /tmp/generator2/
RUN mvn package

FROM getaviz/generator2-base:1.2.1
COPY --from=MAVEN_TOOL_CHAIN /tmp/generator2/target/org.getaviz.generator*.war /var/lib/jetty/webapps/root.war
COPY settings.properties /opt/config/settings.properties
USER root
LABEL maintainer="david.baum@uni-leipzig.de" \
 version="1.0"
