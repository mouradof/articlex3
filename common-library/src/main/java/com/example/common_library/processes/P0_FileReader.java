package com.example.common_library.processes;

import com.example.common_library.utils.StructuredFile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Abstract file reader that processes files and sends parsed entities to a Kafka staging topic.
 * <p>
 * This class reads a file line by line, builds an entity of type {@code T} from the file content,
 * and then produces a Kafka record containing the serialized entity.
 *
 * @param <T> the type of the entity to be processed.
 */
public abstract class P0_FileReader<T> {

    protected StructuredFile fileStructure;
    private final Class<T> entityClass;
    private KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper;

    // Kafka configuration values are injected from the application properties.
    @Value("${KAFKA_BROKER:kafka:9092}")
    private String kafkaBroker;

    @Value("${KAFKA_TOPIC_STAGING:staging_topic}")
    private String stagingTopicName;

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Constructs a file reader with the specified configuration.
     *
     * @param fileStructureDescription the configuration describing the file structure.
     * @param entityClass              the class of the entity to process.
     */
    @Autowired
    public P0_FileReader(StructuredFile fileStructureDescription, Class<T> entityClass) {
        this.fileStructure = fileStructureDescription;
        this.entityClass = entityClass;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Initializes the Kafka producer after the bean is constructed.
     * <p>
     * Throws an IllegalArgumentException if the Kafka broker address is not provided.
     */
    @PostConstruct
    private void initKafkaProducer() {
        if (kafkaBroker == null || kafkaBroker.isEmpty()) {
            log.error("KAFKA_BROKER is not defined or is empty.");
            throw new IllegalArgumentException("KAFKA_BROKER is not defined or is empty.");
        }

        log.info("Initializing Kafka producer with the following parameters:");
        log.info("KAFKA_BROKER: {}", kafkaBroker);
        log.info("KAFKA_TOPIC_STAGING: {}", stagingTopicName);

        Properties producerProps = new Properties();
        producerProps.put("bootstrap.servers", kafkaBroker);
        producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        try {
            this.producer = new KafkaProducer<>(producerProps);
            log.info("Kafka producer initialized successfully.");
        } catch (Exception e) {
            log.error("Error initializing Kafka producer:", e);
            throw e;
        }
    }

    /**
     * Processes the specified file by reading its content, building an entity from each line, and sending
     * the entity to Kafka when a header line is encountered or at the end of the file.
     *
     * @param file the file to process.
     * @throws Exception if an error occurs during processing.
     */
    protected void processFile(File file) throws Exception {
        log.info("Starting processing of file: {}", file.getAbsolutePath());
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            T currentEntity = null;
            int lineNumber = 0;
            int headerLineNumber = 0;

            // Process file line by line.
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (!line.isEmpty()) {
                    String fieldSeparator = fileStructure.getFieldSeparator();
                    if (fieldSeparator == null) {
                        log.error("Field separator (fieldSeparator) is null. Please check the StructuredFile configuration.");
                        throw new IllegalArgumentException("fieldSeparator cannot be null.");
                    }

                    // Split the line into fields.
                    String escapedSeparator = Pattern.quote(fieldSeparator);
                    String[] fields = line.split(escapedSeparator, -1);

                    // Determine the line structure using an identifier from the fields.
                    StructuredFile.StructuredLine lineStructure = fileStructure.getLineStructure("csvline");
                    if (lineStructure == null) {
                        if (fields.length == 0) {
                            log.error("Line {} is empty or improperly formatted.", lineNumber);
                            continue;
                        }
                        String identifier = fields[0];
                        lineStructure = fileStructure.getLineStructure(identifier);
                    }

                    if (lineStructure == null) {
                        log.error("Line structure not found for identifier: {}", fields[0]);
                        throw new Exception("Line structure not found for identifier: " + fields[0]);
                    }

                    // If the line is a header, send the current entity to Kafka and start a new entity.
                    if (lineStructure.isHeader()) {
                        if (currentEntity != null) {
                            ProducerRecord<String, String> recordToSend = createSourceEntityProducerRecord(
                                    currentEntity, file.getName(), headerLineNumber);
                            producer.send(recordToSend, (metadata, exception) -> {
                                if (exception != null) {
                                    log.error("Error sending Kafka record:", exception);
                                } else {
                                    log.debug("Kafka record sent successfully to partition {}, offset {}",
                                            metadata.partition(), metadata.offset());
                                }
                            });
                            log.info("Record sent for entity starting at line {}", headerLineNumber);
                        }
                        headerLineNumber = lineNumber;
                        try {
                            currentEntity = entityClass.getDeclaredConstructor().newInstance();
                            log.debug("New entity initialized at line {}", lineNumber);
                        } catch (Exception e) {
                            log.error("Error instantiating entity at line {}:", lineNumber, e);
                            throw e;
                        }
                    }

                    // Update the current entity with data from the current line.
                    try {
                        createEntitySource(currentEntity, fields, lineStructure);
                        log.debug("Entity updated with fields from line {}", lineNumber);
                    } catch (Exception e) {
                        log.error("Error creating entity at line {}:", lineNumber, e);
                        throw e;
                    }
                } else {
                    log.debug("Line {} is empty. Ignored.", lineNumber);
                }
            }

            // Send the last entity if it exists.
            if (currentEntity != null) {
                ProducerRecord<String, String> recordToSend = createSourceEntityProducerRecord(
                        currentEntity, file.getName(), headerLineNumber);
                producer.send(recordToSend, (metadata, exception) -> {
                    if (exception != null) {
                        log.error("Error sending Kafka record:", exception);
                    } else {
                        log.debug("Kafka record sent successfully to partition {}, offset {}",
                                metadata.partition(), metadata.offset());
                    }
                });
                log.info("Final record sent for entity starting at line {}", headerLineNumber);
            }
        } catch (IOException e) {
            log.error("IO error while reading file {}:", file.getAbsolutePath(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error processing file {}:", file.getAbsolutePath(), e);
            throw e;
        } finally {
            if (producer != null) {
                try {
                    producer.flush();
                    producer.close();
                    log.info("Kafka producer closed.");
                } catch (Exception e) {
                    log.error("Error closing Kafka producer:", e);
                }
            }
        }
    }

    /**
     * Creates a Kafka ProducerRecord for the given entity.
     *
     * @param currentEntity the entity to send.
     * @param sourceName    the name of the source file.
     * @param lineNumber    the line number where the entity began.
     * @return a configured ProducerRecord ready for sending.
     * @throws JsonProcessingException if the entity cannot be serialized to JSON.
     */
    private ProducerRecord<String, String> createSourceEntityProducerRecord(
            T currentEntity, String sourceName, int lineNumber) throws JsonProcessingException {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatterPart1 = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        DateTimeFormatter formatterPart2 = DateTimeFormatter.ofPattern("SSS");
        String identity = entityClass.getSimpleName() + "_" +
                currentDateTime.format(formatterPart1) + "_" + currentDateTime.format(formatterPart2);

        String jsonCurrentEntity = objectMapper.writeValueAsString(currentEntity);

        ProducerRecord<String, String> recordToSend = new ProducerRecord<>(stagingTopicName, jsonCurrentEntity);
        recordToSend.headers().add(new RecordHeader("SOURCE_TYPE", "FILE".getBytes()));
        recordToSend.headers().add(new RecordHeader("SOURCE_NAME", sourceName.getBytes()));
        recordToSend.headers().add(new RecordHeader("SOURCE_LINENBR", String.valueOf(lineNumber).getBytes()));
        recordToSend.headers().add(new RecordHeader("ENTITY_TYPE", entityClass.getSimpleName().getBytes()));
        recordToSend.headers().add(new RecordHeader("ENTITY_ID", identity.getBytes()));

        return recordToSend;
    }

    /**
     * Abstract method to update the current entity with data from a file line.
     *
     * @param currentEntity the current entity being built.
     * @param fields        the array of fields parsed from the line.
     * @param lineStructure the structure definition for the current line.
     */
    protected abstract void createEntitySource(T currentEntity, String[] fields, StructuredFile.StructuredLine lineStructure);
}
