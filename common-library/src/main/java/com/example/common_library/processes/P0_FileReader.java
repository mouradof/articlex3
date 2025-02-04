package com.example.common_library.processes;

import com.example.common_library.utils.TopicNames;
import com.example.common_library.utils.StructuredFile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.regex.Pattern;

public abstract class P0_FileReader<T> {

    protected StructuredFile fileStructure;
    private final Class<T> entityClass;
    protected TopicNames<T> topicNames;

    private KafkaProducer<String, String> producer;
    private ObjectMapper objectMapper;

    private final String kafkaBroker;
    private final String stagingTopicName;

    // Logger SLF4J
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    public P0_FileReader(StructuredFile fileStructureDescription, Class<T> entityClass, TopicNames<T> topicNames,
                         String kafkaBroker, String stagingTopicName) {
        this.fileStructure = fileStructureDescription;
        this.entityClass = entityClass;
        this.topicNames = topicNames;
        this.kafkaBroker = kafkaBroker;
        this.stagingTopicName = stagingTopicName;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    private void initKafkaProducer() {
        if (kafkaBroker == null || kafkaBroker.isEmpty()) {
            log.error("KAFKA_BROKER n'est pas défini ou est vide.");
            throw new IllegalArgumentException("KAFKA_BROKER n'est pas défini ou est vide.");
        }

        // Debug Logs
        log.info("Initialisation du producteur Kafka avec les paramètres suivants :");
        log.info("KAFKA_BROKER: {}", kafkaBroker);
        log.info("KAFKA_TOPIC_STAGING: {}", stagingTopicName);

        Properties producerProps = new Properties();
        producerProps.put("bootstrap.servers", kafkaBroker);
        producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        try {
            this.producer = new KafkaProducer<>(producerProps);
            log.info("Producteur Kafka initialisé avec succès.");
        } catch (Exception e) {
            log.error("Erreur lors de l'initialisation du producteur Kafka :", e);
            throw e;
        }
    }

    protected void processFile(File file) throws Exception {
        log.info("Début du traitement du fichier : {}", file.getAbsolutePath());
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            T currentEntity = null;
            int lineNumber = 0;
            int headerLineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (!line.isEmpty()) {
                    String fieldSeparator = fileStructure.getFieldSeparator();
                    if (fieldSeparator == null) {
                        log.error("Le séparateur de champs (fieldSeparator) est null. Veuillez vérifier la configuration de StructuredFile.");
                        throw new IllegalArgumentException("fieldSeparator ne peut pas être null.");
                    }

                    String escapedSeparator = Pattern.quote(fieldSeparator);
                    String[] fields = line.split(escapedSeparator, -1);

                    StructuredFile.StructuredLine lineStructure = fileStructure.getLineStructure("csvline");
                    if (lineStructure == null) {
                        if (fields.length == 0) {
                            log.error("La ligne {} est vide ou mal formatée.", lineNumber);
                            continue;
                        }
                        String identifier = fields[0];
                        lineStructure = fileStructure.getLineStructure(identifier);
                    }

                    if (lineStructure == null) {
                        log.error("Structure de ligne non trouvée pour l'identifiant: {}", fields[0]);
                        throw new Exception("Structure de ligne non trouvée pour l'identifiant: " + fields[0]);
                    }

                    if (lineStructure.isHeader()) {
                        if (currentEntity != null) {
                            ProducerRecord<String, String> recordToSend = createSourceEntityProducerRecord(
                                    currentEntity, file.getName(), headerLineNumber);
                            producer.send(recordToSend, (metadata, exception) -> {
                                if (exception != null) {
                                    log.error("Erreur lors de l'envoi de l'enregistrement Kafka :", exception);
                                } else {
                                    log.debug("Enregistrement Kafka envoyé avec succès à la partition {}, offset {}", metadata.partition(), metadata.offset());
                                }
                            });
                            log.info("Enregistrement envoyé pour l'entité à la ligne {}", headerLineNumber);
                        }
                        headerLineNumber = lineNumber;
                        try {
                            currentEntity = entityClass.getDeclaredConstructor().newInstance();
                            log.debug("Nouvelle entité initialisée à la ligne {}", lineNumber);
                        } catch (Exception e) {
                            log.error("Erreur lors de l'instanciation de l'entité à la ligne {} :", lineNumber, e);
                            throw e;
                        }
                    }

                    try {
                        createEntitySource(currentEntity, fields, lineStructure);
                        log.debug("Entité mise à jour avec les champs de la ligne {}", lineNumber);
                    } catch (Exception e) {
                        log.error("Erreur lors de la création de l'entité à la ligne {} :", lineNumber, e);
                        throw e;
                    }
                } else {
                    log.debug("Ligne {} est vide. Ignorée.", lineNumber);
                }
            }

            if (currentEntity != null) {
                ProducerRecord<String, String> recordToSend = createSourceEntityProducerRecord(
                        currentEntity, file.getName(), headerLineNumber);
                producer.send(recordToSend, (metadata, exception) -> {
                    if (exception != null) {
                        log.error("Erreur lors de l'envoi de l'enregistrement Kafka :", exception);
                    } else {
                        log.debug("Enregistrement Kafka envoyé avec succès à la partition {}, offset {}", metadata.partition(), metadata.offset());
                    }
                });
                log.info("Dernier enregistrement envoyé pour l'entité à la ligne {}", headerLineNumber);
            }
        } catch (IOException e) {
            log.error("Erreur IO lors de la lecture du fichier {} :", file.getAbsolutePath(), e);
            throw e;
        } catch (Exception e) {
            log.error("Erreur lors du traitement du fichier {} :", file.getAbsolutePath(), e);
            throw e;
        } finally {
            if (producer != null) {
                try {
                    producer.flush();
                    producer.close();
                    log.info("Producteur Kafka fermé.");
                } catch (Exception e) {
                    log.error("Erreur lors de la fermeture du producteur Kafka :", e);
                }
            }
        }
    }

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

    protected abstract void createEntitySource(T currentEntity, String[] fields, StructuredFile.StructuredLine lineStructure);
}
