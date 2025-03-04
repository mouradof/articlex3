package rmn.ETL.stream.process.P3;

import com.example.common_library.processes.P3_Common_LoadProcess;
import com.example.common_library.utils.StructuredFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import rmn.ETL.stream.entities.ARTICLEX3_BEXT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Consumes transformed ARTICLEX3_BEXT messages from Kafka and writes them to an output file.
 * <p>
 * The output file is written in a directory specified by the OUTPUT_DIRECTORY environment variable (or a default value).
 */
@Slf4j
@Service
@Profile("P3")
public class P3_ArticleBext_FileWriter extends P3_Common_LoadProcess<ARTICLEX3_BEXT> implements CommandLineRunner {

    // Output directory for the result file.
    private final String outputDirectory;
    // Fixed name of the output file.
    private final String outputFileName = "bext_final_output.txt";
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
    public P3_ArticleBext_FileWriter(StructuredFile currentFileStructure) {
        super(ARTICLEX3_BEXT.class);
        this.currentFileStructure = currentFileStructure;
        String envOutputDir = System.getenv("OUTPUT_DIRECTORY");
        this.outputDirectory = (envOutputDir == null || envOutputDir.isBlank()) ? "/app/output" : envOutputDir;
        log.info("P3_ArticleBext_FileWriter initialized with outputDirectory: {}", this.outputDirectory);
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
     * Entry point that starts the Kafka consumer process when the application starts.
     * Delegates the Kafka consumption to the common load process.
     *
     * @param args command-line arguments.
     */
    @Override
    public void run(String... args) {
        log.info("Starting Kafka consumer for P3_ArticleBext_FileWriter...");
        super.run();
    }
}
