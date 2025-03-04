FROM gradle:7.6-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build --no-daemon -x test

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# Création du dossier de sortie si besoin
RUN mkdir -p /app/output

EXPOSE 8083

ENV KAFKA_BROKER=localhost:9092
ENV KAFKA_TOPIC_TRANSFORMED=article_manifestationecom_transformed
ENV OUTPUT_DIRECTORY=/app/output
ENV SPRING_PROFILES_ACTIVE=P3

ENTRYPOINT ["java", "-jar", "app.jar"]
