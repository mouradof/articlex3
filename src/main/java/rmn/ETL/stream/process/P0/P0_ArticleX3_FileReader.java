package rmn.ETL.stream.process.P0;

import com.example.common_library.processes.P0_FileReader;
import com.example.common_library.utils.StructuredDataGroup;
import com.example.common_library.utils.StructuredFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import rmn.ETL.stream.entities.ARTICLEX3;

import java.io.File;
import java.util.Arrays;

/**
 * Processes Article X3 files in the P0 profile.
 * <p>
 * This service reads a file, parses its structured lines, creates or updates an ARTICLEX3 entity,
 * and delegates further processing (e.g., sending to a Kafka topic) to the superclass.
 */
@Slf4j
@Service
@Profile("P0")
public class P0_ArticleX3_FileReader extends P0_FileReader<ARTICLEX3> {

    /**
     * Constructs the file reader with the required structured file description.
     *
     * @param structureDescription the description of the file structure
     */
    @Autowired
    public P0_ArticleX3_FileReader(StructuredFile structureDescription) {
        super(structureDescription, ARTICLEX3.class);
    }

    /**
     * Processes the given file by checking its existence and delegating to the superclass.
     *
     * @param file the file to process
     */
    public void processFile(File file) {
        log.info("Processing file: {}", file.getAbsolutePath());
        try {
            if (!file.exists()) {
                log.error("File {} does not exist!", file.getAbsolutePath());
                return;
            }
            super.processFile(file);
        } catch (Exception e) {
            log.error("Error processing file {}: ", file.getAbsolutePath(), e);
        }
    }

    /**
     * Creates or updates the ARTICLEX3 entity based on the line content.
     * <p>
     * This method examines the line type and validates the number of fields. Depending on the type,
     * it either adds a new structured data group or updates an existing one with translation fields.
     *
     * @param articleX3Entity the ARTICLEX3 entity to update
     * @param fileLineFields  the fields from the current line of the file
     * @param lineStructure   the structure definition for the current line type
     */
    @Override
    protected void createEntitySource(ARTICLEX3 articleX3Entity, String[] fileLineFields, StructuredFile.StructuredLine lineStructure) {
        log.debug("lineType={} fields={} (count={})",
                lineStructure.getLineType(),
                Arrays.toString(fileLineFields),
                fileLineFields.length);

        switch (lineStructure.getLineType()) {
            case "I":
                // Process header line: require at least 18 fields.
                if (fileLineFields.length < 18) {
                    log.error("Invalid I line: {} columns -> {}", fileLineFields.length, Arrays.toString(fileLineFields));
                    return;
                }
                articleX3Entity.addStructuredDataGroup(
                        lineStructure.getLineType(),
                        lineStructure.createStructuredDataGroup(fileLineFields)
                );
                break;

            case "M":
                // Process M line: require at least 7 fields.
                if (fileLineFields.length < 7) {
                    log.error("Invalid M line: {} columns -> {}", fileLineFields.length, Arrays.toString(fileLineFields));
                    return;
                }
                articleX3Entity.addStructuredDataGroup(
                        lineStructure.getLineType(),
                        lineStructure.createStructuredDataGroup(fileLineFields)
                );
                break;

            case "ITRD":
                // Process translation for I line: require at least 4 fields.
                if (fileLineFields.length < 4) {
                    log.error("Invalid ITRD line: {} columns -> {}", fileLineFields.length, Arrays.toString(fileLineFields));
                    return;
                }
                if (articleX3Entity.getLastLine("I") != null) {
                    articleX3Entity.getLastLine("I")
                            .addField(
                                    fileLineFields[1],
                                    new StructuredDataGroup.TranslatedField.TranslatedValue(fileLineFields[2], fileLineFields[3])
                            );
                } else {
                    log.warn("No 'I' line found for ITRD");
                }
                break;

            case "MTRD":
                // Process translation for M line: require at least 4 fields.
                if (fileLineFields.length < 4) {
                    log.error("Invalid MTRD line: {} columns -> {}", fileLineFields.length, Arrays.toString(fileLineFields));
                    return;
                }
                if (articleX3Entity.getLastLine("M") != null) {
                    articleX3Entity.getLastLine("M")
                            .addField(
                                    fileLineFields[1],
                                    new StructuredDataGroup.TranslatedField.TranslatedValue(fileLineFields[2], fileLineFields[3])
                            );
                } else {
                    log.warn("No 'M' line found for MTRD");
                }
                break;

            default:
                // For unknown line types, add a new structured data group and log a warning.
                articleX3Entity.addStructuredDataGroup(
                        lineStructure.getLineType(),
                        lineStructure.createStructuredDataGroup(fileLineFields)
                );
                log.warn("Unknown line type: {}", lineStructure.getLineType());
        }
    }
}
