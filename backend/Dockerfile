FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY backend/pom.xml .
COPY backend/.mvn .mvn
COPY backend/mvnw .
COPY backend/src ./src
RUN chmod +x mvnw && ./mvnw package -DskipTests -q
RUN keytool -importcert -alias aiven-ca -file src/main/resources/ca.pem \
    -keystore ca.jks -storepass changeit -storetype JKS -noprompt

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache bash curl
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
RUN mkdir -p /app/uploads/resumes && chown -R appuser:appgroup /app/uploads
COPY --from=build /app/target/job-recruitment-platform-*.jar app.jar
COPY --from=build /app/ca.jks /app/ca.jks
COPY backend/entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh && chown appuser:appgroup app.jar && chown appuser:appgroup /app/ca.jks && chown appuser:appgroup /app/entrypoint.sh
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/health || exit 1
ENTRYPOINT ["/app/entrypoint.sh"]
