# ---- Build Stage ----
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy gradle wrapper first for better caching
COPY gradle/wrapper gradle/wrapper
COPY gradlew .

# Make gradlew executable
RUN chmod +x gradlew

# Copy rest of the files
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY src src

# Build the application
RUN ./gradlew clean bootJar --no-daemon

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Netty tcnative deaktivieren (wichtig bei grpc-netty-shaded)
ENV JAVA_TOOL_OPTIONS="-Dio.grpc.netty.shaded.io.netty.handler.ssl.noOpenSsl=true -Dio.netty.handler.ssl.noOpenSsl=true"

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
