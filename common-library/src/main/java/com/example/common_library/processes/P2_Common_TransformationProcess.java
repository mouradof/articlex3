package com.example.common_library.processes;

import com.example.common_library.utils.TopicNames;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * Common transformation process that consumes validated entities from Kafka,
 * transforms them, and produces the transformed entities to a target Kafka topic.
 * <p>
 * All Kafka configuration and topic references are centralized in this class.
 *
 * @param <T> the source entity type.
 * @param <U> the target entity type.
 */
public abstract class P2_Common_TransformationProcess<T, U> {

    private static final Logger logger = LoggerFactory.getLogger(P2_Common_TransformationProcess.class);

    @Autowired
    protected TopicNames<U> topicNames;

    private final Class<T> sourceEntityClass;
    private final Class<U> targetEntityClass;

    private boolean keepRunning = true;

    public P2_Common_TransformationProcess(Class<T> sourceEntityClass, Class<U> targetEntityClass) {
        this.sourceEntityClass = sourceEntityClass;
        this.targetEntityClass = targetEntityClass;
    }

    public void stop() {
        keepRunning = false;
    }

    public void run() {
        Properties config = loadConfig();
        Properties consumerConfig = loadConsumerConfig(config);
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, this.getClass().getSimpleName());
        Properties producerConfig = loadProducerConfig(config);

        String sourceTopic = topicNames.getValidatedTopicName();
        String targetTopic = topicNames.getTargetTopicName();

        logger.info("Source topic: {}", sourceTopic);
        logger.info("Target topic: {}", targetTopic);

        if (targetTopic == null || targetTopic.isBlank()) {
            logger.error("Error: target topic is null or empty!");
            return;
        }

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerConfig);
             KafkaProducer<String, String> producer = new KafkaProducer<>(producerConfig)) {

            consumer.subscribe(Collections.singletonList(sourceTopic));
            ObjectMapper objectMapper = new ObjectMapper();

            while (keepRunning) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    processRecord(record, producer, targetTopic, objectMapper);
                }
            }
        } catch (Exception e) {
            logger.error("Error during Kafka process", e);
        }
    }

    private void processRecord(ConsumerRecord<String, String> record,
                               KafkaProducer<String, String> producer,
                               String targetTopic,
                               ObjectMapper objectMapper) {

        try {
            T sourceEntity = objectMapper.readValue(record.value(), sourceEntityClass);
            U targetEntity = transformToTargetEntity(sourceEntity);

            if (targetEntity != null) {
                sendTargetEntity(producer, targetTopic, targetEntity, record.offset(), objectMapper);
            }
        } catch (Exception e) {
            logger.error("Error during transformation", e);
        }
    }

    private void sendTargetEntity(KafkaProducer<String, String> producer,
                                  String targetTopic,
                                  U targetEntity,
                                  long offset,
                                  ObjectMapper objectMapper) {
        try {
            String targetEntityJson = objectMapper.writeValueAsString(targetEntity);
            ProducerRecord<String, String> targetRecord = new ProducerRecord<>(targetTopic, targetEntityJson);
            targetRecord.headers().add(new RecordHeader("ENTITY_TYPE", targetEntityClass.getSimpleName().getBytes()));
            producer.send(targetRecord).get();
            logger.info("Message sent to {}: {}", targetTopic, targetEntityJson);

        } catch (Exception e) {
            logger.error("Error sending Kafka message", e);
        }
    }

    /**
     * Loads Kafka configuration properties.
     * <p>
     * All Kafka configuration is centralized in this method.
     *
     * @return the Kafka configuration properties.
     */
    protected Properties loadConfig() {
        Properties props = new Properties();
        String bootstrapServers = System.getenv("KAFKA_BROKER");
        if (bootstrapServers == null || bootstrapServers.isBlank()) {
            bootstrapServers = "kafka:9092";
        }
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        logger.info("Kafka Properties: {}", props);
        return props;
    }

    private Properties loadConsumerConfig(Properties config) {
        Properties consumerConfig = new Properties();
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                config.getOrDefault("bootstrap.servers", "kafka:9092"));
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                config.getOrDefault("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer"));
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                config.getOrDefault("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer"));
        consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                config.getOrDefault("auto.offset.reset", "earliest"));

        logger.info("Kafka Consumer Properties: {}", consumerConfig);

        return consumerConfig;
    }

    private Properties loadProducerConfig(Properties config) {
        Properties producerConfig = new Properties();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                config.getOrDefault("bootstrap.servers", "kafka:9092"));
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                config.getOrDefault("key.serializer", "org.apache.kafka.common.serialization.StringSerializer"));
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                config.getOrDefault("value.serializer", "org.apache.kafka.common.serialization.StringSerializer"));

        logger.info("Kafka Producer Properties: {}", producerConfig);

        return producerConfig;
    }

    /**
     * Transforms the given source entity to a target entity.
     *
     * @param sourceEntity the source entity to transform.
     * @return the transformed target entity.
     */
    public abstract U transformToTargetEntity(T sourceEntity);
}
