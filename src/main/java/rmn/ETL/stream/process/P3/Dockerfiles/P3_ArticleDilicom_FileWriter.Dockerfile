FROM gradle:7.6-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build --no-daemon

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8083

ENV KAFKA_BROKER=localhost:9092
ENV INPUT_TOPIC=article_output
ENV OUTPUT_FILE=/app/output/dilicom_final_output.txt

ENTRYPOINT ["java", "-jar", "app.jar"]