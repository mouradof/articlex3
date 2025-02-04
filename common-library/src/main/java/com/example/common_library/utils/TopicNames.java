package com.example.common_library.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component  // Indique que cette classe est un composant Spring et peut être injectée dans d'autres classes
public class TopicNames<T> {

    private Class<T> targetEntityClass;  // La classe de l'entité cible (générique)

    // Déclaration des topics Kafka avec des valeurs par défaut (qui peuvent être remplacées par des propriétés externes)
    @Value("${kafka.topics.staging:STAGING_DATA}")
    private String stagingTopic;  // Topic pour les données en staging

    @Value("${kafka.topics.validated:VALIDATED_DATA}")
    private String validatedTopic;  // Topic pour les données validées

    @Value("${kafka.topics.rejected:REJECTED_DATA}")
    private String rejectedTopic;  // Topic pour les données rejetées

    // Constructeur sans arguments
    public TopicNames() {
    }

    // Constructeur avec la classe d'entité cible
    public TopicNames(Class<T> targetEntityClass) {
        this.targetEntityClass = targetEntityClass;
    }

    // Retourne le nom du topic de staging
    public String getStagingTopicName() {
        return stagingTopic;
    }

    // Retourne le nom du topic validé
    public String getValidatedTopicName() {
        return validatedTopic;
    }

    // Retourne le nom du topic rejeté
    public String getRejectedTopicName() {
        return rejectedTopic;
    }

    // Retourne le nom du topic cible basé sur le nom de la classe d'entité
    public String getTargetTopicName() {
        String topicName = "";
        // Si la classe cible est non nulle, on génère un nom de topic basé sur le nom de la classe
        if (targetEntityClass != null) {
            String className = targetEntityClass.getSimpleName();
            // Si le nom de la classe contient un "_", on extrait la deuxième partie après l'underscore pour le nom du topic
            if (className.contains("_")) {
                topicName = "TARGET_" + className.split("_")[1];
            }
        }
        return topicName;  // Retourne le nom du topic généré ou vide si la classe cible est nulle
    }
}
