FROM maven:3.6.0-jdk-8-alpine AS MAVEN_TOOL_CHAIN
ADD https://jqassistant.org/wp-content/uploads/2018/12/jqassistant-commandline-neo4jv3-1.6.0-distribution.zip /opt/
RUN mkdir -p /tmp/generator2/ && cd /opt && unzip jqassistant-commandline-neo4jv3-1.6.0-distribution.zip
COPY . /tmp/generator2/
WORKDIR /tmp/generator2/
RUN mvn package

FROM jetty:9.4.12-jre8-alpine
COPY --from=MAVEN_TOOL_CHAIN /tmp/generator2/target/org.getaviz.generator*.war /var/lib/jetty/webapps/root.war
COPY --from=MAVEN_TOOL_CHAIN /opt/jqassistant-commandline-neo4jv3-1.6.0/ /opt/jqassistant/
COPY start.sh /start.sh
COPY settings.properties /opt/config/settings.properties
RUN mkdir -p /var/lib/jetty/data-gen/ && mkdir -p /var/lib/jetty/logs/ && mkdir -p /var/lib/jetty/output/ && chown -R jetty:jetty /var/lib/jetty/logs/ /var/lib/jetty/output/
EXPOSE 8080
ENTRYPOINT ["/start.sh"]
USER root
RUN sed -i 's/\/home\/jetty:\/sbin\/nologin/\/home\/jetty:\/bin\/sh/g' /etc/passwd
VOLUME ["/var/lib/jetty/webapps/", "/opt/config/", "/var/lib/jetty/output/", "/var/lib/jetty/logs/", "/var/lib/jetty/data-gen/"]
LABEL maintainer="david.baum@uni-leipzig.de" \
 version="1.0"
