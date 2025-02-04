package com.example.common_library.processes;

import com.example.common_library.utils.TopicNames;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Lazy;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

@Lazy
@ConditionalOnMissingBean(P2_Common_TransformationProcess.class)
@SuppressWarnings("unused")
public abstract class P2_Common_TransformationProcess<T, U> {

    private static final Logger logger = LoggerFactory.getLogger(P2_Common_TransformationProcess.class);

    private final Class<T> sourceEntityClass;
    private final Class<U> targetEntityClass;

    @Autowired
    private TopicNames<U> topicNames;

    // Constructeur qui reçoit les types des entités source et cible pour la transformation
    public P2_Common_TransformationProcess(Class<T> sourceEntityClass, Class<U> targetEntityClass) {
        this.sourceEntityClass = sourceEntityClass;
        this.targetEntityClass = targetEntityClass;
    }

    // Flag pour arrêter le processus en cours
    private boolean keepRunning = true;

    // Méthode pour stopper le processus de transformation
    public void stop() {
        keepRunning = false;
    }

    // Méthode principale qui démarre le processus de transformation
    public void run() {
        Properties config = loadConfig();  // Charge la configuration globale
        Properties consumerConfig = loadConsumerConfig(config);  // Charge la configuration du consommateur
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, this.getClass().getSimpleName());  // Définit le groupId du consommateur
        Properties producerConfig = loadProducerConfig(config);  // Charge la configuration du producteur

        // Récupère les noms des topics à partir de TopicNames
        String sourceTopic = topicNames.getValidatedTopicName();  // Topic source validé
        String targetTopic = topicNames.getTargetTopicName();  // Topic cible pour les entités transformées

        logger.info("TOPIC source: {}", sourceTopic);
        logger.info("TOPIC cible: {}", targetTopic);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerConfig);
             KafkaProducer<String, String> producer = new KafkaProducer<>(producerConfig)) {

            consumer.subscribe(Collections.singletonList(sourceTopic));  // Le consommateur s'abonne au topic source
            ObjectMapper objectMapper = new ObjectMapper();  // Objet pour la sérialisation et désérialisation JSON

            // Tant que le processus est en cours, poll les messages du topic
            while (keepRunning) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));  // Récupère les messages
                for (ConsumerRecord<String, String> record : records) {
                    processRecord(record, producer, targetTopic, objectMapper);  // Traite chaque message
                }
            }
        } catch (Exception e) {
            logger.error("Error in transformation process", e);  // Capture les erreurs éventuelles
        }
    }

    // Méthode qui traite chaque enregistrement Kafka récupéré
    private void processRecord(ConsumerRecord<String, String> record, KafkaProducer<String, String> producer,
                               String targetTopic, ObjectMapper objectMapper) {
        String sourceEntityJson = record.value();  // Données de l'entité source en format JSON
        String sourceEntityType = "";
        String sourceIdEntity = "";

        // Récupère les en-têtes pour extraire le type et l'ID de l'entité source
        for (Header header : record.headers()) {
            switch (header.key()) {
                case "ENTITY_TYPE" -> sourceEntityType = new String(header.value());
                case "ENTITY_ID" -> sourceIdEntity = new String(header.value());
            }
        }

        // Si le type d'entité correspond à celui de l'entité source, on effectue la transformation
        if (sourceEntityType.equals(sourceEntityClass.getSimpleName())) {
            try {
                // Désérialisation de l'entité source depuis le JSON
                T sourceEntity = objectMapper.readValue(sourceEntityJson, sourceEntityClass);
                // Transformation de l'entité source vers l'entité cible
                U targetEntity = transformToTargetEntity(sourceEntity);

                if (targetEntity != null) {
                    // Envoi de l'entité cible vers le topic de destination
                    sendTargetEntity(producer, targetTopic, targetEntity, sourceIdEntity, record.offset(), objectMapper);
                }
            } catch (JsonProcessingException e) {
                logger.error("Error deserializing source entity", e);  // Gestion des erreurs de désérialisation
            }
        }
    }

    // Méthode qui envoie l'entité cible transformée vers le topic de destination
    private void sendTargetEntity(KafkaProducer<String, String> producer, String targetTopic, U targetEntity,
                                  String sourceIdEntity, long offset, ObjectMapper objectMapper) {
        try {
            String targetEntityJson = objectMapper.writeValueAsString(targetEntity);  // Sérialisation de l'entité cible en JSON

            ProducerRecord<String, String> targetRecord = new ProducerRecord<>(targetTopic, targetEntityJson);

            // Ajout des en-têtes à l'enregistrement Kafka
            targetRecord.headers().add(new RecordHeader("ENTITY_TYPE", targetEntityClass.getSimpleName().getBytes()));
            targetRecord.headers().add(new RecordHeader("SOURCE_TYPE", "TOPIC".getBytes()));
            targetRecord.headers().add(new RecordHeader("SOURCE_NAME", topicNames.getValidatedTopicName().getBytes()));
            targetRecord.headers().add(new RecordHeader("SOURCE_OFFSET", String.valueOf(offset).getBytes()));
            targetRecord.headers().add(new RecordHeader("ENTITY_ID", sourceIdEntity.getBytes()));

            producer.send(targetRecord);  // Envoi de l'enregistrement vers le topic cible
            logger.info("Record sent to topic {}: {}", targetTopic, targetEntityJson);  // Log l'envoi
        } catch (JsonProcessingException e) {
            logger.error("Error serializing target entity", e);  // Gestion des erreurs de sérialisation
        }
    }

    // Méthode qui charge la configuration générale
    private Properties loadConfig() {
        return new Properties();
    }

    // Méthode qui charge la configuration du consommateur Kafka
    private Properties loadConsumerConfig(Properties config) {
        Properties consumerConfig = new Properties();
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getProperty("bootstrap.servers"));
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, config.getProperty("key.deserializer"));
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, config.getProperty("value.deserializer"));
        consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.getProperty("auto.offset.reset"));
        return consumerConfig;
    }

    // Méthode qui charge la configuration du producteur Kafka
    private Properties loadProducerConfig(Properties config) {
        Properties producerConfig = new Properties();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getProperty("bootstrap.servers"));
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, config.getProperty("key.serializer"));
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, config.getProperty("value.serializer"));
        return producerConfig;
    }

    // Méthode abstraite pour effectuer la transformation de l'entité source vers l'entité cible
    public abstract U transformToTargetEntity(T sourceEntity);
}
