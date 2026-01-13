FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

COPY FoundBuddy-backend/gradle gradle
COPY FoundBuddy-backend/gradlew .
COPY FoundBuddy-backend/build.gradle.kts .
COPY FoundBuddy-backend/settings.gradle.kts .
COPY FoundBuddy-backend/src .

RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

