package com.example.common_library.processes;

import com.example.common_library.utils.TopicNames;
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
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Service
public abstract class P1_Common_ValidationProcess<T> {

    private final Class<T> stagingEntityClass;
    private static final Logger logger = LoggerFactory.getLogger(P1_Common_ValidationProcess.class);

    @Autowired
    protected TopicNames<T> topicNames;

    @Autowired
    private ObjectMapper objectMapper;

    public P1_Common_ValidationProcess(Class<T> stagingEntityClass) {
        this.stagingEntityClass = stagingEntityClass;
    }

    @Async
    public void runProcess() {
        Properties config = loadConfig();  // Chargement de la configuration
        Properties stagingConsumerConfig = loadConsumerConfig(config);  // Chargement des configurations pour le consommateur Kafka

        stagingConsumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, this.getClass().getSimpleName());

        String idEntity = "";

        String stagingTopic = topicNames.getStagingTopicName();  // Topic source de staging
        String validatedTopic = topicNames.getValidatedTopicName();  // Topic pour les entités validées
        String rejectedTopic = topicNames.getRejectedTopicName();  // Topic pour les entités rejetées

        logger.info("TOPIC source: {}", stagingTopic);
        logger.info("TOPIC cible validation: {}", validatedTopic);
        logger.info("TOPIC cible rejet: {}", rejectedTopic);

        long maxPollInterval = Long.parseLong(stagingConsumerConfig.getProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000"));

        try (KafkaConsumer<String, String> stagingConsumer = new KafkaConsumer<>(stagingConsumerConfig);
             KafkaProducer<String, String> producer = new KafkaProducer<>(loadProducerConfig(config))) {

            stagingConsumer.subscribe(Collections.singletonList(stagingTopic));

            while (true) {
                ConsumerRecords<String, String> records = stagingConsumer.poll(Duration.ofMillis(maxPollInterval));

                for (ConsumerRecord<String, String> record : records) {
                    processRecord(record, producer, validatedTopic, rejectedTopic, idEntity);
                }
            }
        } catch (Exception e) {
            logger.error("Error during process execution", e);  // Capture les erreurs pendant le processus
        }
    }

    private void processRecord(ConsumerRecord<String, String> record, KafkaProducer<String, String> producer,
                               String validatedTopic, String rejectedTopic, String idEntity) {
        String kafkaStagingData = record.value();
        int retryCount = 0;
        String entityType = "";

        for (Header header : record.headers()) {
            switch (header.key()) {
                case "RETRY_COUNT" -> retryCount = Integer.parseInt(new String(header.value()));
                case "ENTITY_TYPE" -> entityType = new String(header.value());
                case "ENTITY_ID" -> idEntity = new String(header.value());
            }
        }

        if (entityType.trim().equals(stagingEntityClass.getSimpleName().trim())) {
            try {
                T entity = deserializeFromJSON(kafkaStagingData, stagingEntityClass);
                List<String> errorMessages = validateEntitySource(entity);

                if (errorMessages.isEmpty()) {
                    sendValidatedRecord(producer, validatedTopic, record, entity, idEntity);
                } else {
                    sendRejectedRecord(producer, rejectedTopic, record, entity, idEntity, retryCount, errorMessages);
                }
            } catch (Exception e) {
                logger.error("Error processing record", e);
            }
        }
    }

    private void sendValidatedRecord(KafkaProducer<String, String> producer, String validatedTopic,
                                     ConsumerRecord<String, String> record, T entity, String idEntity) {
        String validatedJsonEntity = serialiseToJSON(entity);
        ProducerRecord<String, String> validatedRecordToSend = new ProducerRecord<>(validatedTopic, validatedJsonEntity);

        // Ajoute des en-têtes standards à l'enregistrement
        addStandardHeadersToRecord(validatedRecordToSend, idEntity, record.offset());
        producer.send(validatedRecordToSend);  // Envoie l'enregistrement validé
        logger.info("Offset validated: {}", record.offset());
    }

    // Méthode qui envoie un enregistrement rejeté sur Kafka
    private void sendRejectedRecord(KafkaProducer<String, String> producer, String rejectedTopic,
                                    ConsumerRecord<String, String> record, T entity, String idEntity, int retryCount,
                                    List<String> errorMessages) {
        String rejectedJsonEntity = serialiseToJSON(entity);  // Sérialise l'entité en JSON
        ProducerRecord<String, String> rejectedRecord = new ProducerRecord<>(rejectedTopic, rejectedJsonEntity);

        // Ajoute des en-têtes standards et d'autres en-têtes spécifiques aux enregistrements rejetés
        addStandardHeadersToRecord(rejectedRecord, idEntity, record.offset());
        rejectedRecord.headers().add(new RecordHeader("RETRY_DELAY", "10000".getBytes()));  // Ajoute un délai de réessai
        rejectedRecord.headers().add(new RecordHeader("RETRY_COUNT", String.valueOf(retryCount).getBytes()));

        // Ajoute les messages d'erreur
        for (String errorMessage : errorMessages) {
            rejectedRecord.headers().add(new RecordHeader("ERROR_LIBELLE", errorMessage.getBytes()));
        }

        producer.send(rejectedRecord);  // Envoie l'enregistrement rejeté
        logger.info("Offset rejected: {}", record.offset());
    }

    // Méthode qui ajoute des en-têtes standards aux enregistrements Kafka
    private void addStandardHeadersToRecord(ProducerRecord<String, String> record, String idEntity, long sourceOffset) {
        record.headers().add(new RecordHeader("SOURCE_TYPE", "TOPIC".getBytes()));
        record.headers().add(new RecordHeader("SOURCE_NAME", "STAGING_DATA".getBytes()));
        record.headers().add(new RecordHeader("SOURCE_OFFSET", String.valueOf(sourceOffset).getBytes()));
        record.headers().add(new RecordHeader("ENTITY_TYPE", stagingEntityClass.getSimpleName().getBytes()));
        record.headers().add(new RecordHeader("ENTITY_ID", idEntity.getBytes()));
    }

    // Sérialisation d'un objet en JSON
    private String serialiseToJSON(T objectToSerialize) {
        try {
            return objectMapper.writeValueAsString(objectToSerialize);  // Sérialisation de l'entité
        } catch (Exception e) {
            logger.error("Error serializing to JSON", e);
            return null;
        }
    }

    // Désérialisation d'une chaîne JSON en objet de type T
    private T deserializeFromJSON(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);  // Désérialisation
        } catch (Exception e) {
            logger.error("Error deserializing JSON", e);
            return null;
        }
    }

    // Chargement de la configuration générale
    private Properties loadConfig() {
        Properties properties = new Properties();
        // Vous pouvez charger des configurations supplémentaires ici si nécessaire
        return properties;
    }

    // Chargement de la configuration du consommateur Kafka
    private Properties loadConsumerConfig(Properties config) {
        Properties consumerConfig = new Properties();
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getProperty("spring.kafka.bootstrap-servers", "localhost:9092"));
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, config.getProperty("spring.kafka.consumer.key-deserializer", "org.apache.kafka.common.serialization.StringDeserializer"));
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, config.getProperty("spring.kafka.consumer.value-deserializer", "org.apache.kafka.common.serialization.StringDeserializer"));
        consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.getProperty("spring.kafka.consumer.auto-offset-reset", "earliest"));
        consumerConfig.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, config.getProperty("spring.kafka.consumer.max-poll-interval-ms", "300000"));
        return consumerConfig;
    }

    // Chargement de la configuration du producteur Kafka
    private Properties loadProducerConfig(Properties config) {
        Properties producerConfig = new Properties();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getProperty("spring.kafka.bootstrap-servers", "localhost:9092"));
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, config.getProperty("spring.kafka.producer.key-serializer", "org.apache.kafka.common.serialization.StringSerializer"));
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, config.getProperty("spring.kafka.producer.value-serializer", "org.apache.kafka.common.serialization.StringSerializer"));
        return producerConfig;
    }

    // Méthode abstraite qui doit être implémentée pour valider l'entité source
    public abstract List<String> validateEntitySource(T sourceEntity);
}
