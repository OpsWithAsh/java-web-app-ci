FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY target/java-web-app-ci-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar", "--server.address=0.0.0.0"]
