package rmn.ETL.stream.process.P3;

import com.example.common_library.processes.P3_Common_LoadProcess;
import com.example.common_library.utils.StructuredFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import rmn.ETL.stream.entities.ARTICLEX3_BEXT;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * Consumes transformed ARTICLEX3_BEXT messages from Kafka and writes them to an output file.
 * <p>
 * The file is written to a directory specified by the OUTPUT_DIRECTORY environment variable (or a default value).
 */
@Slf4j
@Service
public class P3_ArticleBext_FileWriter extends P3_Common_LoadProcess<ARTICLEX3_BEXT> implements CommandLineRunner {

    // Output directory for the result file
    private final String outputDirectory;
    // Fixed name of the output file
    private final String outputFileName = "bext_final_output.txt";
    // File structure configuration (separator, EOL, etc.)
    private final StructuredFile currentFileStructure;
    // ObjectMapper for JSON deserialization
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Constructor with file structure injection.
     * Reads the OUTPUT_DIRECTORY environment variable with a default value.
     *
     * @param currentFileStructure the configuration for the file structure.
     */
    public P3_ArticleBext_FileWriter(StructuredFile currentFileStructure) {
        super(ARTICLEX3_BEXT.class);
        this.currentFileStructure = currentFileStructure;
        String envOutputDir = System.getenv("OUTPUT_DIRECTORY");
        this.outputDirectory = (envOutputDir == null || envOutputDir.isBlank()) ? "/app/output" : envOutputDir;
        log.info("P3_ArticleBext_FileWriter initialized with outputDirectory: {}", this.outputDirectory);
    }

    /**
     * Loads Kafka consumer configuration properties.
     *
     * @return the Kafka consumer properties.
     */
    @Override
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
        log.info("Kafka bootstrap.servers set to {}", bootstrapServers);
        return props;
    }

    /**
     * Formats an ARTICLEX3_BEXT entity into a string based on the file structure.
     *
     * @param articleBext   the entity to format.
     * @param fileStructure the file structure configuration.
     * @return the formatted string.
     */
    protected String formatTargetEntity(ARTICLEX3_BEXT articleBext, StructuredFile fileStructure) {
        try {
            StringBuilder fileContent = new StringBuilder();

            fileContent.append(getSafeValue(articleBext.getReference())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getEan13())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getCodeRMN())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getDesignation())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getLibEtiquette())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getCodeVie())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getDebutVie())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getFinVie())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getCleGL())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getLibCleGL())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getCodeFournisseur())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getNomFournisseur())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getPoidsERP())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getCodeDouane())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getCategorieArticle())).append(fileStructure.getEOL());

            log.info("Formatted ARTICLEX3_BEXT entity: {}", fileContent);
            return fileContent.toString();
        } catch (Exception e) {
            log.error("Error formatting ARTICLEX3_BEXT entity: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Returns a safe string value, avoiding nulls.
     *
     * @param value the input value.
     * @return the original value or an empty string if null.
     */
    private String getSafeValue(String value) {
        return value != null ? value : "";
    }

    /**
     * Writes the transformed entity to the output file.
     *
     * @param targetEntity the entity to write.
     */
    @Override
    public void loadTargetEntity(ARTICLEX3_BEXT targetEntity) {
        log.info("Writing entity: {}", targetEntity);
        String formattedLine = formatTargetEntity(targetEntity, currentFileStructure);
        String fullOutputFilePath = outputDirectory + "/" + outputFileName;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fullOutputFilePath, true))) {
            writer.write(formattedLine);
            writer.newLine();
            writer.flush();
            log.info("Entity written to file: {}", targetEntity.getReference());
        } catch (IOException e) {
            log.error("Error writing entity to file: {}", e.getMessage());
        }
    }

    /**
     * Logs initialization details after the bean is constructed.
     */
    @PostConstruct
    public void debugInitialization() {
        String fullPath = outputDirectory + "/" + outputFileName;
        log.info("P3_ArticleBext_FileWriter started with outputDirectory: {} and file name: {}",
                outputDirectory, outputFileName);
        log.info("Full output file path: {}", fullPath);
    }

    /**
     * Starts the Kafka consumer process when the application starts.
     *
     * @param args command-line arguments.
     */
    @Override
    public void run(String... args) {
        log.info("Starting Kafka consumer for P3_ArticleBext_FileWriter...");
        runProcess();
    }

    /**
     * Consumes messages from Kafka and writes them to the output file.
     */
    private void runProcess() {
        Properties props = loadConfig();
        String topic = System.getenv("KAFKA_TOPIC_TRANSFORMED");
        if (topic == null || topic.isBlank()) {
            topic = "article_bext_transformed";
        }
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));
            log.info("Subscribed to Kafka topic: {}", topic);
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    log.info("Received message: key={}, value={}", record.key(), record.value());
                    ARTICLEX3_BEXT entity = convertRecordToEntity(record.value());
                    if (entity != null) {
                        loadTargetEntity(entity);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error in Kafka consumer loop: {}", e.getMessage());
        }
    }

    /**
     * Converts a JSON message into an ARTICLEX3_BEXT entity.
     *
     * @param recordValue the JSON string.
     * @return the converted entity or null if conversion fails.
     */
    private ARTICLEX3_BEXT convertRecordToEntity(String recordValue) {
        try {
            return objectMapper.readValue(recordValue, ARTICLEX3_BEXT.class);
        } catch (Exception e) {
            log.error("Error converting record to ARTICLEX3_BEXT: {}", e.getMessage());
            return null;
        }
    }
}
