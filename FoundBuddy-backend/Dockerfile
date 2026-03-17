# ---- Build Stage ----
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy gradle wrapper first for better caching
COPY gradle/wrapper gradle/wrapper
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./

# Make gradlew executable and test gradle
RUN chmod +x gradlew
RUN ./gradlew --version

# Copy source code
COPY src src

# Build the application with more memory
RUN ./gradlew clean bootJar --no-daemon -Dorg.gradle.jvmargs=-Xmx1g

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Netty tcnative deaktivieren (wichtig bei grpc-netty-shaded)
ENV JAVA_TOOL_OPTIONS="-Dio.grpc.netty.shaded.io.netty.handler.ssl.noOpenSsl=true -Dio.netty.handler.ssl.noOpenSsl=true"
ENV SERVER_PORT=7860

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 7860
ENTRYPOINT ["java","-jar","app.jar"]
