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
ENV KAFKA_TOPIC_STAGING=article_staging
ENV KAFKA_TOPIC_VALIDATED=article_validated
ENV KAFKA_TOPIC_REJECTED=article_rejected
ENV SPRING_PROFILES_ACTIVE=P2
ENV KAFKA_TOPIC_TRANSFORMED=article_ecom_transformed
ENTRYPOINT ["java", "-jar", "app.jar"]