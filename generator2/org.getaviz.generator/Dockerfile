FROM jetty:9.4.12-jre8
ADD target/org.getaviz.generator*.war /var/lib/jetty/webapps/root.war
USER root
RUN mkdir -p /usr/local/jetty/logs
EXPOSE 8080
VOLUME ["/var/lib/jetty/webapps/", "/opt/config/", "/opt/databases/"]
LABEL maintainer="david.baum@uni-leipzig.de" \
 version="1.0"
