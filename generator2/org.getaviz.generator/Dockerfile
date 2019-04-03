FROM maven:3.6.0-jdk-8 AS MAVEN_TOOL_CHAIN
RUN mkdir -p /tmp/generator2/
COPY pom.xml /tmp/generator2/
COPY src /tmp/generator2/src/
ADD https://jqassistant.org/wp-content/uploads/2018/12/jqassistant-commandline-neo4jv3-1.6.0-distribution.zip /opt/
RUN cd /opt && unzip jqassistant-commandline-neo4jv3-1.6.0-distribution.zip
WORKDIR /tmp/generator2/
RUN mvn package

FROM jetty:9.4.12-jre8
COPY --from=MAVEN_TOOL_CHAIN /tmp/generator2/target/org.getaviz.generator*.war /var/lib/jetty/webapps/root.war
COPY --from=MAVEN_TOOL_CHAIN /opt/jqassistant-commandline-neo4jv3-1.6.0/ /opt/jqassistant/
RUN mkdir -p /var/lib/jetty/data-gen/ && mkdir -p /var/lib/jetty/logs/ && mkdir -p /var/lib/jetty/output/
EXPOSE 8080
VOLUME ["/var/lib/jetty/webapps/", "/opt/config/", "/var/lib/jetty/output/", "/var/lib/jetty/data-gen/"]
LABEL maintainer="david.baum@uni-leipzig.de" \
 version="1.0"
