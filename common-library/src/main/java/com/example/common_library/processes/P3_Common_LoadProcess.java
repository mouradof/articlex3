package com.example.common_library.processes;

import com.example.common_library.utils.TopicNames;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

@SuppressWarnings("unused")
public abstract class P3_Common_LoadProcess<U> {

    // Logger pour consigner les informations et les erreurs
    private static final Logger logger = LoggerFactory.getLogger(P3_Common_LoadProcess.class);

    // Déclaration de la classe de l'entité cible
    private final Class<U> targetEntityClass;

    // Injection dynamique des noms de topics via TopicNames
    @Autowired
    private TopicNames<U> topicNames;

    // Constructeur qui initialise la classe de l'entité cible
    public P3_Common_LoadProcess(Class<U> targetEntityClass) {
        this.targetEntityClass = targetEntityClass;
    }

    // Flag pour contrôler l'exécution continue du processus
    private boolean keepRunning = true;

    // Méthode pour arrêter l'exécution du processus
    public void stop() {
        keepRunning = false;
    }

    // Méthode principale qui lance le processus de chargement
    public void run() {
        // Chargement des configurations
        Properties config = loadConfig();
        Properties consumerConfig = loadConsumerConfig(config);

        // Assignation du groupId dynamique basé sur le nom de la classe
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, this.getClass().getSimpleName());

        // Récupération du nom du topic cible depuis TopicNames
        String targetTopic = topicNames.getTargetTopicName();
        logger.info("TOPIC cible: {}", targetTopic);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerConfig)) {
            // Abonnement au topic cible
            consumer.subscribe(Collections.singletonList(targetTopic));
            ObjectMapper objectMapper = new ObjectMapper();

            // Consommation des messages tant que `keepRunning` est vrai
            while (keepRunning) {
                // Poll des messages depuis Kafka
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                // Traitement de chaque enregistrement consommé
                for (ConsumerRecord<String, String> record : records) {
                    processRecord(record, objectMapper);
                }
            }
        } catch (Exception e) {
            // Gestion des erreurs survenues pendant le traitement
            logger.error("Error in load process", e);
        }
    }

    // Méthode qui traite chaque enregistrement Kafka
    private void processRecord(ConsumerRecord<String, String> record, ObjectMapper objectMapper) {
        // Récupération du JSON de l'entité cible depuis la valeur du record
        String targetEntityJson = record.value();
        String sourceEntityId = "";

        // Extraction des en-têtes du message Kafka
        for (Header header : record.headers()) {
            if ("ENTITY_ID".equals(header.key())) {
                sourceEntityId = new String(header.value());
            }
        }

        try {
            // Désérialisation du JSON en entité cible
            U targetEntity = objectMapper.readValue(targetEntityJson, targetEntityClass);

            if (targetEntity != null) {
                // Si l'entité est valide, appel de la méthode abstraite pour charger l'entité
                loadTargetEntity(targetEntity);
                logger.info("Entity loaded successfully: ID={}", sourceEntityId);
            } else {
                // Si l'entité est invalide ou null, on consigne un avertissement
                logger.warn("Skipped loading entity: invalid or null entity for ID={}", sourceEntityId);
            }
        } catch (Exception e) {
            // Gestion des erreurs de désérialisation ou autres erreurs lors du traitement
            logger.error("Failed to load entity for ID={}", sourceEntityId, e);
        }
    }

    // Chargement de la configuration générique
    private Properties loadConfig() {
        return new Properties();
    }

    // Chargement de la configuration du consommateur Kafka
    private Properties loadConsumerConfig(Properties config) {
        Properties consumerConfig = new Properties();
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getProperty("bootstrap.servers"));
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, config.getProperty("key.deserializer"));
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, config.getProperty("value.deserializer"));
        consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.getProperty("auto.offset.reset"));
        return consumerConfig;
    }

    /**
     * Méthode abstraite qui doit être implémentée par les sous-classes pour charger une entité cible.
     *
     * @param targetEntity l'entité cible à charger
     */
    public abstract void loadTargetEntity(U targetEntity);
}
