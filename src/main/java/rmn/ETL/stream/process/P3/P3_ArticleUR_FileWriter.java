package rmn.ETL.stream.process.P3;

import com.example.common_library.processes.P3_Common_LoadProcess;
import com.example.common_library.utils.StructuredFile;
import org.springframework.context.annotation.Profile;
import rmn.ETL.stream.entities.ARTICLEX3_UR;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

/**
 * Consumes transformed ARTICLEX3_UR messages from Kafka and writes them as XML to an output file.
 * <p>
 * The output file is written in a directory specified by the OUTPUT_DIRECTORY environment variable (or a default value).
 */
@Slf4j
@Service
@Profile("P3")
public class P3_ArticleUR_FileWriter extends P3_Common_LoadProcess<ARTICLEX3_UR> implements CommandLineRunner {

    // Output directory for the result file.
    private final String outputDirectory;
    // Fixed name of the output file.
    private final String outputFileName = "ur_final_output.xml";
    // File structure configuration (not used for XML formatting, but may be needed for consistency).
    private final StructuredFile currentFileStructure;
    // ObjectMapper for XML serialization.
    private final XmlMapper xmlMapper = new XmlMapper();

    /**
     * Constructor with file structure injection.
     *
     * @param currentFileStructure the configuration for the file structure.
     */
    public P3_ArticleUR_FileWriter(StructuredFile currentFileStructure) {
        super(ARTICLEX3_UR.class);
        this.currentFileStructure = currentFileStructure;
        String envOutputDir = System.getenv("OUTPUT_DIRECTORY");
        this.outputDirectory = (envOutputDir == null || envOutputDir.isBlank()) ? "/app/output" : envOutputDir;
        log.info("P3_ArticleUR_FileWriter initialized with outputDirectory: {}", this.outputDirectory);
    }

    /**
     * Formats an ARTICLEX3_UR entity into an XML string.
     *
     * @param articleUR the entity to format.
     * @param fileStructure the file structure configuration (unused here).
     * @return the formatted XML string.
     */
    protected String formatTargetEntity(ARTICLEX3_UR articleUR, StructuredFile fileStructure) {
        try {
            String xmlContent = xmlMapper.writeValueAsString(articleUR);
            log.info("Formatted ARTICLEX3_UR entity as XML: {}", xmlContent);
            return xmlContent;
        } catch (Exception e) {
            log.error("Error formatting ARTICLEX3_UR entity to XML: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Writes the transformed ARTICLEX3_UR entity (in XML) to the output file.
     *
     * @param targetEntity the entity to write.
     */
    @Override
    public void loadTargetEntity(ARTICLEX3_UR targetEntity) {
        log.info("Writing ARTICLEX3_UR entity: {}", targetEntity);
        String formattedContent = formatTargetEntity(targetEntity, currentFileStructure);
        String fullOutputFilePath = outputDirectory + "/" + outputFileName;
        try (var writer = new java.io.BufferedWriter(new java.io.FileWriter(fullOutputFilePath, true))) {
            writer.write(formattedContent);
            writer.newLine();
            writer.flush();
            log.info("Entity written to file: {}", targetEntity.getCode());
        } catch (Exception e) {
            log.error("Error writing ARTICLEX3_UR entity to file: {}", e.getMessage());
        }
    }

    /**
     * Logs initialization details after the bean is constructed.
     */
    @PostConstruct
    public void debugInitialization() {
        String fullPath = outputDirectory + "/" + outputFileName;
        log.info("P3_ArticleUR_FileWriter started with outputDirectory: {} and file name: {}",
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
        log.info("Starting Kafka consumer for P3_ArticleUR_FileWriter...");
        super.run();
    }
}
