FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/job-recruitment-platform-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
