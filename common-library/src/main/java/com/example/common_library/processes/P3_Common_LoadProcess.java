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

/**
 * Common load process that consumes target entities from Kafka and passes them to the
 * subclass for further processing (e.g. writing to a file).
 * <p>
 * All Kafka configuration and topic references are handled here.
 *
 * @param <U> the target entity type.
 */
public abstract class P3_Common_LoadProcess<U> {

    private static final Logger logger = LoggerFactory.getLogger(P3_Common_LoadProcess.class);

    // Class of the target entity.
    private final Class<U> targetEntityClass;

    // Injection of topic names via TopicNames.
    @Autowired
    private TopicNames<U> topicNames;

    public P3_Common_LoadProcess(Class<U> targetEntityClass) {
        this.targetEntityClass = targetEntityClass;
    }

    // Flag to control continuous execution of the process.
    private boolean keepRunning = true;

    public void stop() {
        keepRunning = false;
    }

    /**
     * Main method that launches the Kafka consumption process.
     */
    public void run() {
        Properties config = loadConfig();
        Properties consumerConfig = loadConsumerConfig(config);
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, this.getClass().getSimpleName());

        // Retrieve the target topic from the injected TopicNames.
        String targetTopic = topicNames.getTargetTopicName();
        logger.info("Target topic: {}", targetTopic);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerConfig)) {
            consumer.subscribe(Collections.singletonList(targetTopic));
            ObjectMapper objectMapper = new ObjectMapper();

            while (keepRunning) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    logger.info("Received message: {}", record.value());
                    processRecord(record, objectMapper);
                }
            }
        } catch (Exception e) {
            logger.error("Error in load process", e);
        }
    }

    /**
     * Processes each Kafka record by deserializing the JSON and invoking loadTargetEntity.
     *
     * @param record       the Kafka record.
     * @param objectMapper for JSON deserialization.
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
     * Loads the Kafka configuration properties.
     * <p>
     * This default implementation centralizes the Kafka configuration.
     *
     * @return Properties for the KafkaConsumer.
     */
    protected Properties loadConfig() {
        Properties props = new Properties();
        String bootstrapServers = System.getenv("KAFKA_BROKER");
        if (bootstrapServers == null || bootstrapServers.isBlank()) {
            bootstrapServers = "localhost:9092";
        }
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", "p3-articlebext-filewriter");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
        logger.info("Kafka bootstrap.servers set to {}", bootstrapServers);
        return props;
    }

    private Properties loadConsumerConfig(Properties config) {
        Properties consumerConfig = new Properties();
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getProperty("bootstrap.servers"));
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, config.getProperty("key.deserializer"));
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, config.getProperty("value.deserializer"));
        consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.getProperty("auto.offset.reset"));
        return consumerConfig;
    }

    /**
     * Abstract method to be implemented by subclasses to process (load) a target entity.
     *
     * @param targetEntity the target entity to load.
     */
    public abstract void loadTargetEntity(U targetEntity);
}
