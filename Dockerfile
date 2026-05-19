FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/UserService-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${SPR_PROFILE}", "app.jar"]