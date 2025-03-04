package rmn.ETL.stream.process.P1;

import com.example.common_library.processes.P1_Common_ValidationProcess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import rmn.ETL.stream.entities.ARTICLEX3;

import java.util.ArrayList;
import java.util.List;

/**
 * Validation process for ARTICLEX3 entities.
 * <p>
 * This service validates ARTICLEX3 entities by ensuring required fields are present
 * and that certain field values are unique. It is executed as a CommandLineRunner under the "P1" profile.
 */
@Slf4j
@Service
@Profile("P1")
public class P1_ArticleX3_ValidationProcess extends P1_Common_ValidationProcess<ARTICLEX3> implements CommandLineRunner {

    /**
     * Default constructor.
     * <p>
     * All Kafka topic references and configurations are handled in the superclass.
     */
    public P1_ArticleX3_ValidationProcess() {
        super(ARTICLEX3.class);
    }

    /**
     * Validates the provided ARTICLEX3 entity.
     * <p>
     * This method checks if the entity contains structured data groups, validates that each "I" line
     * has a non-empty 'ITMREF' field, and verifies that the 'ITMREF' values are unique.
     *
     * @param entity the ARTICLEX3 entity to validate.
     * @return a list of error messages, or an empty list if no validation errors are found.
     */
    @Override
    public List<String> validateEntitySource(ARTICLEX3 entity) {
        List<String> errors = new ArrayList<>();

        // Check if the entity contains any structured data groups.
        if (entity.getStructuredDataGroups().isEmpty()) {
            errors.add("No data found in ARTICLEX3 entity.");
        }

        // Validate that each "I" line contains a non-empty 'ITMREF' field.
        entity.getLines("I").forEach(line -> {
            String reference = line.getDataGroup().getFieldValue("ITMREF");
            if (reference == null || reference.isEmpty()) {
                errors.add("Missing 'ITMREF' field in line type 'I'.");
            }
        });

        // Collect all 'ITMREF' values from "I" lines and check for duplicates.
        List<String> itemReferences = entity.getLines("I").stream()
                .map(line -> line.getDataGroup().getFieldValue("ITMREF"))
                .toList();
        if (itemReferences.size() != itemReferences.stream().distinct().count()) {
            errors.add("Duplicate 'ITMREF' values found in line type 'I'.");
        }

        log.debug("Validation completed with {} errors.", errors.size());
        return errors;
    }

    /**
     * Entry point for the validation process.
     * <p>
     * Invoked at application startup to trigger the validation process.
     *
     * @param args command-line arguments.
     * @throws Exception if an error occurs during processing.
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("Starting P1_ArticleX3_ValidationProcess...");
        runProcess();
    }
}
