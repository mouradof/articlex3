package rmn.ETL.stream.process.P3;

import com.example.common_library.processes.P3_Common_LoadProcess;
import com.example.common_library.utils.StructuredFile;
import org.springframework.context.annotation.Profile;
import rmn.ETL.stream.entities.MANIFESTATION_ECOM;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Consumes transformed MANIFESTATION_ECOM messages from Kafka and writes them to an output file.
 * <p>
 * The output file is written in a directory specified by the OUTPUT_DIRECTORY environment variable (or a default value).
 */
@Slf4j
@Service
@Profile("P3")
public class P3_ManifestationEcom_FileWriter extends P3_Common_LoadProcess<MANIFESTATION_ECOM> implements CommandLineRunner {

    // Output directory for the result file.
    private final String outputDirectory;
    // Fixed name of the output file.
    private final String outputFileName = "manifestation_ecom_final_output.txt";
    // File structure configuration (separator, EOL, etc.).
    private final StructuredFile currentFileStructure;
    // ObjectMapper for JSON deserialization.
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Constructor with file structure injection.
     *
     * @param currentFileStructure the configuration for the file structure.
     */
    public P3_ManifestationEcom_FileWriter(StructuredFile currentFileStructure) {
        super(MANIFESTATION_ECOM.class);
        this.currentFileStructure = currentFileStructure;
        String envOutputDir = System.getenv("OUTPUT_DIRECTORY");
        this.outputDirectory = (envOutputDir == null || envOutputDir.isBlank()) ? "/app/output" : envOutputDir;
        log.info("P3_ManifestationEcom_FileWriter initialized with outputDirectory: {}", this.outputDirectory);
    }

    /**
     * Formats a MANIFESTATION_ECOM entity into a string based on the file structure.
     *
     * For this target, we output the following fields:
     * - CodeManifestation, LibelleManifestation, DateDebut, DateFin.
     *
     * @param manifestationEcom the entity to format.
     * @param fileStructure     the file structure configuration.
     * @return the formatted string.
     */
    protected String formatTargetEntity(MANIFESTATION_ECOM manifestationEcom, StructuredFile fileStructure) {
        if (manifestationEcom == null) {
            log.warn("MANIFESTATION_ECOM entity is null. Skipping formatting.");
            return "";
        }
        try {
            String fieldSeparator = fileStructure.getFieldSeparator();
            String eol = fileStructure.getEOL();
            StringBuilder fileContent = new StringBuilder();

            fileContent.append(getSafeValue(manifestationEcom.getCodeManifestation())).append(fieldSeparator)
                    .append(getSafeValue(manifestationEcom.getLibelleManifestation())).append(fieldSeparator)
                    .append(getSafeValue(manifestationEcom.getDateDebut())).append(fieldSeparator)
                    .append(getSafeValue(manifestationEcom.getDateFin())).append(eol);

            log.info("Formatted MANIFESTATION_ECOM entity: {}", fileContent);
            return fileContent.toString();
        } catch (Exception e) {
            log.error("Error while formatting MANIFESTATION_ECOM entity: {}", e.getMessage());
            return "";
        }
    }

    private String getSafeValue(String value) {
        return value != null ? value : "";
    }

    /**
     * Writes the transformed MANIFESTATION_ECOM entity to the output file.
     *
     * @param targetEntity the entity to write.
     */
    @Override
    public void loadTargetEntity(MANIFESTATION_ECOM targetEntity) {
        log.info("Writing MANIFESTATION_ECOM entity: {}", targetEntity);
        String formattedLine = formatTargetEntity(targetEntity, currentFileStructure);
        String fullOutputFilePath = outputDirectory + "/" + outputFileName;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fullOutputFilePath, true))) {
            writer.write(formattedLine);
            writer.newLine();
            writer.flush();
            log.info("Entity written to file: {}", targetEntity.getCodeManifestation());
        } catch (IOException e) {
            log.error("Error writing MANIFESTATION_ECOM entity to file: {}", e.getMessage());
        }
    }

    /**
     * Logs initialization details after the bean is constructed.
     */
    @PostConstruct
    public void debugInitialization() {
        String fullPath = outputDirectory + "/" + outputFileName;
        log.info("P3_ManifestationEcom_FileWriter started with outputDirectory: {} and file name: {}",
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
        log.info("Starting Kafka consumer for P3_ManifestationEcom_FileWriter...");
        super.run();
    }
}
