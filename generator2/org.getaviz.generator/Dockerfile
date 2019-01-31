FROM maven:3.6.0-jdk-8 AS MAVEN_TOOL_CHAIN
RUN mkdir -p /tmp/generator2/
COPY pom.xml /tmp/generator2/
COPY src /tmp/generator2/src/
WORKDIR /tmp/generator2/
RUN mvn package

FROM jetty:9.4.12-jre8
COPY --from=MAVEN_TOOL_CHAIN /tmp/generator2/target/org.getaviz.generator*.war /var/lib/jetty/webapps/root.war
RUN mkdir -p /var/lib/jetty/logs/
RUN mkdir -p /var/lib/jetty/databases/
RUN mkdir -p /var/lib/jetty/output/
EXPOSE 8080
VOLUME ["/var/lib/jetty/webapps/", "/opt/config/", "/var/lib/jetty/databases/", "/var/lib/jetty/output/"]
LABEL maintainer="david.baum@uni-leipzig.de" \
 version="1.0"
