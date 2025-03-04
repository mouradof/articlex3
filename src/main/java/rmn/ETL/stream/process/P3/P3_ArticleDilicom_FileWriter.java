package rmn.ETL.stream.process.P3;

import com.example.common_library.processes.P3_Common_LoadProcess;
import com.example.common_library.utils.StructuredFile;
import org.springframework.context.annotation.Profile;
import rmn.ETL.stream.entities.ARTICLE_DILICOM;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Consumes transformed ARTICLE_DILICOM messages from Kafka and writes them to an output file.
 * <p>
 * The output file is written in a directory specified by the OUTPUT_DIRECTORY environment variable (or a default value).
 */
@Slf4j
@Service
@Profile("P3")
public class P3_ArticleDilicom_FileWriter extends P3_Common_LoadProcess<ARTICLE_DILICOM> implements CommandLineRunner {

    // Output directory for the result file.
    private final String outputDirectory;
    // Fixed name of the output file.
    private final String outputFileName = "dilicom_final_output.txt";
    // File structure configuration (separator, EOL, etc.).
    private final StructuredFile currentFileStructure;
    // ObjectMapper for JSON deserialization.
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Constructor with file structure injection.
     * Reads the OUTPUT_DIRECTORY environment variable with a default value.
     *
     * @param currentFileStructure the configuration for the file structure.
     */
    public P3_ArticleDilicom_FileWriter(StructuredFile currentFileStructure) {
        super(ARTICLE_DILICOM.class);
        this.currentFileStructure = currentFileStructure;
        String envOutputDir = System.getenv("OUTPUT_DIRECTORY");
        this.outputDirectory = (envOutputDir == null || envOutputDir.isBlank()) ? "/app/output" : envOutputDir;
        log.info("P3_ArticleDilicom_FileWriter initialized with outputDirectory: {}", this.outputDirectory);
    }

    /**
     * Formats an ARTICLE_DILICOM entity into a string based on the file structure.
     *
     * For this target, we output the following fields:
     * - Action, EAN13, GLN13.
     *
     * @param articleDilicom the entity to format.
     * @param fileStructure  the file structure configuration.
     * @return the formatted string.
     */
    protected String formatTargetEntity(ARTICLE_DILICOM articleDilicom, StructuredFile fileStructure) {
        try {
            StringBuilder fileContent = new StringBuilder();

            fileContent.append(getSafeValue(articleDilicom.getAction()))
                    .append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleDilicom.getEan13()))
                    .append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleDilicom.getGln13()))
                    .append(fileStructure.getEOL());

            log.info("Formatted ARTICLE_DILICOM entity: {}", fileContent);
            return fileContent.toString();
        } catch (Exception e) {
            log.error("Error formatting ARTICLE_DILICOM entity: {}", e.getMessage());
            return "";
        }
    }

    private String getSafeValue(String value) {
        return value != null ? value : "";
    }

    /**
     * Writes the transformed ARTICLE_DILICOM entity to the output file.
     *
     * @param targetEntity the entity to write.
     */
    @Override
    public void loadTargetEntity(ARTICLE_DILICOM targetEntity) {
        log.info("Writing ARTICLE_DILICOM entity: {}", targetEntity);
        String formattedLine = formatTargetEntity(targetEntity, currentFileStructure);
        String fullOutputFilePath = outputDirectory + "/" + outputFileName;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fullOutputFilePath, true))) {
            writer.write(formattedLine);
            writer.newLine();
            writer.flush();
            log.info("Entity written to file: {}", targetEntity.getAction()); // ou un autre identifiant
        } catch (IOException e) {
            log.error("Error writing ARTICLE_DILICOM entity to file: {}", e.getMessage());
        }
    }

    /**
     * Logs initialization details after the bean is constructed.
     */
    @PostConstruct
    public void debugInitialization() {
        String fullPath = outputDirectory + "/" + outputFileName;
        log.info("P3_ArticleDilicom_FileWriter started with outputDirectory: {} and file name: {}",
                outputDirectory, outputFileName);
        log.info("Full output file path: {}", fullPath);
    }

    /**
     * Entry point that starts the Kafka consumer process when the application starts.
     *
     * @param args command-line arguments.
     */
    @Override
    public void run(String... args) {
        log.info("Starting Kafka consumer for P3_ArticleDilicom_FileWriter...");
        super.run();
    }
}
