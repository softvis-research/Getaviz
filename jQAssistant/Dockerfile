# Alpine Linux with OpenJDK JRE
FROM getaviz/jqassistant-base:development
RUN mkdir -p /tmp/build/
COPY . /tmp/build/
WORKDIR /tmp/build/
RUN mvn package && cp /tmp/build/target/org.getaviz.jqassistant-1.0.0-SNAPSHOT.war /getaviz-jqassistant.war && rm -Rf /tmp/build
# run the app
CMD ["/usr/bin/java", "-jar", "/getaviz-jqassistant.war"]
