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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Abstract common validation process that consumes staged entities from Kafka,
 * validates them, and then produces them to either a validated or a rejected topic.
 * <p>
 * All Kafka topic references and configurations are handled in this class.
 *
 * @param <T> the type of the entity to be validated.
 */
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
        Properties config = loadConfig();  // Load general configuration (includes bootstrap servers from environment variables)
        Properties stagingConsumerConfig = loadConsumerConfig(config);  // Load Kafka consumer configuration

        stagingConsumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, this.getClass().getSimpleName());

        // Retrieve topic names, giving priority to environment variables if defined
        String stagingTopic = System.getenv("KAFKA_TOPIC_STAGING");
        if (stagingTopic == null) {
            stagingTopic = topicNames.getStagingTopicName();
        }

        String validatedTopic = System.getenv("KAFKA_TOPIC_VALIDATED");
        if (validatedTopic == null) {
            validatedTopic = topicNames.getValidatedTopicName();
        }

        String rejectedTopic = System.getenv("KAFKA_TOPIC_REJECTED");
        if (rejectedTopic == null) {
            rejectedTopic = topicNames.getRejectedTopicName();
        }

        logger.info("Source topic: {}", stagingTopic);
        logger.info("Validated topic: {}", validatedTopic);
        logger.info("Rejected topic: {}", rejectedTopic);

        long maxPollInterval = Long.parseLong(stagingConsumerConfig.getProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "300000"));

        try (KafkaConsumer<String, String> stagingConsumer = new KafkaConsumer<>(stagingConsumerConfig);
             KafkaProducer<String, String> producer = new KafkaProducer<>(loadProducerConfig(config))) {

            stagingConsumer.subscribe(Collections.singletonList(stagingTopic));

            while (true) {
                ConsumerRecords<String, String> records = stagingConsumer.poll(Duration.ofMillis(maxPollInterval));

                for (ConsumerRecord<String, String> record : records) {
                    processRecord(record, producer, validatedTopic, rejectedTopic, "");
                }
            }
        } catch (Exception e) {
            logger.error("Error during process execution", e);
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

        addStandardHeadersToRecord(validatedRecordToSend, idEntity, record.offset());
        producer.send(validatedRecordToSend);
        logger.info("Offset validated: {}", record.offset());
    }

    private void sendRejectedRecord(KafkaProducer<String, String> producer, String rejectedTopic,
                                    ConsumerRecord<String, String> record, T entity, String idEntity, int retryCount,
                                    List<String> errorMessages) {
        String rejectedJsonEntity = serialiseToJSON(entity);
        ProducerRecord<String, String> rejectedRecord = new ProducerRecord<>(rejectedTopic, rejectedJsonEntity);

        addStandardHeadersToRecord(rejectedRecord, idEntity, record.offset());
        rejectedRecord.headers().add(new RecordHeader("RETRY_DELAY", "10000".getBytes()));
        rejectedRecord.headers().add(new RecordHeader("RETRY_COUNT", String.valueOf(retryCount).getBytes()));

        for (String errorMessage : errorMessages) {
            rejectedRecord.headers().add(new RecordHeader("ERROR_LIBELLE", errorMessage.getBytes()));
        }

        producer.send(rejectedRecord);
        logger.info("Offset rejected: {}", record.offset());
    }

    private void addStandardHeadersToRecord(ProducerRecord<String, String> record, String idEntity, long sourceOffset) {
        record.headers().add(new RecordHeader("SOURCE_TYPE", "TOPIC".getBytes()));
        record.headers().add(new RecordHeader("SOURCE_NAME", "STAGING_DATA".getBytes()));
        record.headers().add(new RecordHeader("SOURCE_OFFSET", String.valueOf(sourceOffset).getBytes()));
        record.headers().add(new RecordHeader("ENTITY_TYPE", stagingEntityClass.getSimpleName().getBytes()));
        record.headers().add(new RecordHeader("ENTITY_ID", idEntity.getBytes()));
    }

    private String serialiseToJSON(T objectToSerialize) {
        try {
            return objectMapper.writeValueAsString(objectToSerialize);
        } catch (Exception e) {
            logger.error("Error serializing to JSON", e);
            return null;
        }
    }

    private T deserializeFromJSON(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            logger.error("Error deserializing JSON", e);
            return null;
        }
    }

    // Load general configuration, including bootstrap servers from environment variables
    private Properties loadConfig() {
        Properties properties = new Properties();
        String bootstrapServers = System.getenv("SPRING_KAFKA_BOOTSTRAP_SERVERS");
        if (bootstrapServers != null) {
            properties.setProperty("spring.kafka.bootstrap-servers", bootstrapServers);
        }
        return properties;
    }

    private Properties loadConsumerConfig(Properties config) {
        Properties consumerConfig = new Properties();
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                config.getProperty("spring.kafka.bootstrap-servers", "localhost:9092"));
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                config.getProperty("spring.kafka.consumer.key-deserializer", "org.apache.kafka.common.serialization.StringDeserializer"));
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                config.getProperty("spring.kafka.consumer.value-deserializer", "org.apache.kafka.common.serialization.StringDeserializer"));
        consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                config.getProperty("spring.kafka.consumer.auto-offset-reset", "earliest"));
        consumerConfig.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG,
                config.getProperty("spring.kafka.consumer.max-poll-interval-ms", "300000"));
        return consumerConfig;
    }

    private Properties loadProducerConfig(Properties config) {
        Properties producerConfig = new Properties();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                config.getProperty("spring.kafka.bootstrap-servers", "localhost:9092"));
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                config.getProperty("spring.kafka.producer.key-serializer", "org.apache.kafka.common.serialization.StringSerializer"));
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                config.getProperty("spring.kafka.producer.value-serializer", "org.apache.kafka.common.serialization.StringSerializer"));
        return producerConfig;
    }

    /**
     * Abstract method to validate the provided source entity.
     *
     * @param sourceEntity the entity to validate.
     * @return a list of error messages, or an empty list if no validation errors are found.
     */
    public abstract List<String> validateEntitySource(T sourceEntity);
}
