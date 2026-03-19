FROM gradle:8.14.3-jdk17 AS base

WORKDIR /workspace
COPY . .
RUN chmod +x ./gradlew

FROM base AS tester
RUN ./gradlew --no-daemon test

FROM base AS builder
RUN ./gradlew --no-daemon bootJar -x test

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
