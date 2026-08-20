FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/case-backend-core-server-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
