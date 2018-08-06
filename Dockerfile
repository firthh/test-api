FROM openjdk:8-jre

COPY target/server.jar /app/server.jar
WORKDIR /app
CMD ["java", "-jar", "server.jar"]
