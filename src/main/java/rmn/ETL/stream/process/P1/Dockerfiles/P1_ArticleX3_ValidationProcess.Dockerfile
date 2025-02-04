# Étape d'exécution
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copier le JAR construit depuis le sous-projet p1
COPY p1/build/libs/ArticleX3-0.0.1-SNAPSHOT-p1.jar app.jar

# Exposer le port si nécessaire (ex. 8080 pour Spring Boot)
EXPOSE 8080

# Définir les variables d'environnement avec des valeurs par défaut
ENV KAFKA_BROKER=kafka:9092
ENV KAFKA_TOPIC_STAGING=article_validated
ENV KAFKA_TOPIC_VALIDATED=article_transformed
ENV KAFKA_TOPIC_REJECTED=article_rejected

# Définir le point d'entrée de l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
