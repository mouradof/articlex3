FROM gradle:7.6-jdk17 AS builder
WORKDIR /app
COPY ./build.gradle.kts ./settings.gradle.kts ./
COPY ./common-library ./common-library
COPY ./src ./src
RUN gradle clean build --no-daemon --info -x test

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENV INPUT_DIR=/app/input
ENV OUTPUT_TOPIC=article_staging
ENV SPRING_PROFILES_ACTIVE=P0
ENTRYPOINT ["java", "-jar", "app.jar"]
