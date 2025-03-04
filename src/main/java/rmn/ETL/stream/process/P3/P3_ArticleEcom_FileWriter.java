package rmn.ETL.stream.process.P3;

import com.example.common_library.processes.P3_Common_LoadProcess;
import com.example.common_library.utils.StructuredFile;
import org.springframework.context.annotation.Profile;
import rmn.ETL.stream.entities.ARTICLEX3_ECOM;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Consumes transformed ARTICLEX3_ECOM messages from Kafka and writes them to an output file.
 * <p>
 * The output file is written in a directory specified by the OUTPUT_DIRECTORY environment variable (or a default value).
 */
@Slf4j
@Service
@Profile("P3")
public class P3_ArticleEcom_FileWriter extends P3_Common_LoadProcess<ARTICLEX3_ECOM> implements CommandLineRunner {

    // Output directory for the result file.
    private final String outputDirectory;
    // Fixed name of the output file.
    private final String outputFileName = "ecom_final_output.txt";
    // File structure configuration (separator, EOL, etc.).
    private final StructuredFile currentFileStructure;
    // ObjectMapper for JSON deserialization.
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Constructor with file structure injection.
     *
     * @param currentFileStructure the configuration for the file structure.
     */
    public P3_ArticleEcom_FileWriter(StructuredFile currentFileStructure) {
        super(ARTICLEX3_ECOM.class);
        this.currentFileStructure = currentFileStructure;
        String envOutputDir = System.getenv("OUTPUT_DIRECTORY");
        this.outputDirectory = (envOutputDir == null || envOutputDir.isBlank()) ? "/app/output" : envOutputDir;
        log.info("P3_ArticleEcom_FileWriter initialized with outputDirectory: {}", this.outputDirectory);
    }

    /**
     * Formats an ARTICLEX3_ECOM entity into a string based on the file structure.
     *
     * For this target, we output the following fields:
     * - ReferenceRMN, EAN, Designation1, MarqueEditeur, StatutX3, PoidsEmballe, LibelleLigneDediee, CodesManifestation (joined by ";"), TechniqueImpression.
     *
     * @param articleEcom   the entity to format.
     * @param fileStructure the file structure configuration.
     * @return the formatted string.
     */
    protected String formatTargetEntity(ARTICLEX3_ECOM articleEcom, StructuredFile fileStructure) {
        try {
            StringBuilder fileContent = new StringBuilder();

            fileContent.append(getSafeValue(articleEcom.getReferenceRMN())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleEcom.getEan())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleEcom.getDesignation1())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleEcom.getMarqueEditeur())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleEcom.getStatutX3())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleEcom.getPoidsEmballe())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleEcom.getLibelleLigneDediee())).append(fileStructure.getFieldSeparator())
                    .append(String.join(";", articleEcom.getCodesManifestation())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleEcom.getTechniqueImpression())).append(fileStructure.getEOL());

            log.info("Formatted ARTICLEX3_ECOM entity: {}", fileContent);
            return fileContent.toString();
        } catch (Exception e) {
            log.error("Error formatting ARTICLEX3_ECOM entity: {}", e.getMessage());
            return "";
        }
    }

    private String getSafeValue(String value) {
        return value != null ? value : "";
    }

    /**
     * Writes the transformed ARTICLEX3_ECOM entity to the output file.
     *
     * @param targetEntity the entity to write.
     */
    @Override
    public void loadTargetEntity(ARTICLEX3_ECOM targetEntity) {
        log.info("Writing ARTICLEX3_ECOM entity: {}", targetEntity);
        String formattedLine = formatTargetEntity(targetEntity, currentFileStructure);
        String fullOutputFilePath = outputDirectory + "/" + outputFileName;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fullOutputFilePath, true))) {
            writer.write(formattedLine);
            writer.newLine();
            writer.flush();
            log.info("Entity written to file: {}", targetEntity.getReferenceRMN());
        } catch (IOException e) {
            log.error("Error writing ARTICLEX3_ECOM entity to file: {}", e.getMessage());
        }
    }

    /**
     * Logs initialization details after the bean is constructed.
     */
    @PostConstruct
    public void debugInitialization() {
        String fullPath = outputDirectory + "/" + outputFileName;
        log.info("P3_ArticleEcom_FileWriter started with outputDirectory: {} and file name: {}",
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
        log.info("Starting Kafka consumer for P3_ArticleEcom_FileWriter...");
        super.run();
    }
}
