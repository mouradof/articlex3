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

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

@SuppressWarnings("unused")
public abstract class P3_Common_LoadProcess<U> {

    private static final Logger logger = LoggerFactory.getLogger(P3_Common_LoadProcess.class);

    // Classe de l'entité cible
    private final Class<U> targetEntityClass;

    // Injection dynamique des noms de topics via TopicNames
    @Autowired
    private TopicNames<U> topicNames;

    public P3_Common_LoadProcess(Class<U> targetEntityClass) {
        this.targetEntityClass = targetEntityClass;
    }

    // Flag pour contrôler l'exécution continue du processus
    private boolean keepRunning = true;

    public void stop() {
        keepRunning = false;
    }

    /**
     * Méthode principale qui lance le processus de consommation et de traitement.
     */
    public void run() {
        Properties config = loadConfig();
        Properties consumerConfig = loadConsumerConfig(config);
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, this.getClass().getSimpleName());

        String targetTopic = topicNames.getTargetTopicName();
        logger.info("TOPIC cible: {}", targetTopic);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerConfig)) {
            consumer.subscribe(Collections.singletonList(targetTopic));
            ObjectMapper objectMapper = new ObjectMapper();

            while (keepRunning) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    // Log pour vérifier la réception de messages
                    logger.info("Message reçu : {}", record.value());
                    processRecord(record, objectMapper);
                }
            }
        } catch (Exception e) {
            logger.error("Error in load process", e);
        }
    }

    /**
     * Méthode qui traite chaque enregistrement Kafka.
     *
     * @param record       l'enregistrement Kafka
     * @param objectMapper pour désérialiser le JSON
     */
    private void processRecord(ConsumerRecord<String, String> record, ObjectMapper objectMapper) {
        String targetEntityJson = record.value();
        String sourceEntityId = "";

        for (Header header : record.headers()) {
            if ("ENTITY_ID".equals(header.key())) {
                sourceEntityId = new String(header.value());
            }
        }

        try {
            U targetEntity = objectMapper.readValue(targetEntityJson, targetEntityClass);

            if (targetEntity != null) {
                loadTargetEntity(targetEntity);
                logger.info("Entity loaded successfully: ID={}", sourceEntityId);
            } else {
                logger.warn("Skipped loading entity: invalid or null entity for ID={}", sourceEntityId);
            }
        } catch (Exception e) {
            logger.error("Failed to load entity for ID={}", sourceEntityId, e);
        }
    }

    /**
     * Méthode abstraite à surcharger pour fournir la configuration Kafka.
     *
     * @return Properties pour le KafkaConsumer.
     */
    protected abstract Properties loadConfig();

    private Properties loadConsumerConfig(Properties config) {
        Properties consumerConfig = new Properties();
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getProperty("bootstrap.servers"));
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, config.getProperty("key.deserializer"));
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, config.getProperty("value.deserializer"));
        consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.getProperty("auto.offset.reset"));
        return consumerConfig;
    }

    /**
     * Méthode abstraite à implémenter par les sous-classes pour charger une entité.
     *
     * @param targetEntity l'entité cible à charger
     */
    public abstract void loadTargetEntity(U targetEntity);
}
