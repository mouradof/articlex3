package rmn.ETL.stream.config;

import com.example.common_library.utils.StructuredFile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Configures the structure of the article X3 file.
 * <p>
 * This configuration defines multiple line structures with specific field names
 * and a field separator. The helper method {@code createLine} is used to reduce code duplication.
 */
@Configuration
public class StructuredFileConfig {

    /**
     * Builds and returns a StructuredFile configured with predefined line structures.
     *
     * @return a StructuredFile instance with lines for 'I', 'ITRD', 'M', and 'MTRD'
     */
    @Bean
    public StructuredFile articleX3FileStructure() {
        StructuredFile fileStructure = new StructuredFile();

        // Add the 'I' line structure (mandatory)
        fileStructure.addLineStructure(createLine("I", true, new String[] {
                "TCLCOD", "YCLEALCE", "ITMREF", "EANCOD", "DES1AXX", "DES2AXX", "DES3AXX", "ITMSTA", "YDISPCDE",
                "LIFSTRDAT", "LIFENDDAT", "YPARDAT", "YUNITE", "STU", "WEU", "ITMWEI", "YPOIDSNU", "DES1"
        }));

        // Add the 'ITRD' line structure (optional)
        fileStructure.addLineStructure(createLine("ITRD", false, new String[] {
                "FIELDNAME", "LANG", "TRADUCTION"
        }));

        // Add the 'M' line structure (optional)
        fileStructure.addLineStructure(createLine("M", false, new String[] {
                "ILLUST", "FACON", "PAGIN", "NBPHOTO", "WEIPAP1", "WEICOUV"
        }));

        // Add the 'MTRD' line structure (optional)
        fileStructure.addLineStructure(createLine("MTRD", false, new String[] {
                "FACON", "LANG", "TRADUCTION"
        }));

        // Set the field separator used in the file
        fileStructure.setFieldSeparator("|");

        return fileStructure;
    }

    /**
     * Creates a new StructuredLine with the specified type, mandatory flag, and field names.
     *
     * @param lineType  the identifier for the line structure.
     * @param mandatory flag indicating whether the line is mandatory.
     * @param fields    an array of field names for this line.
     * @return a configured StructuredLine instance.
     */
    private StructuredFile.StructuredLine createLine(String lineType, boolean mandatory, String[] fields) {
        StructuredFile.StructuredLine line = new StructuredFile.StructuredLine(lineType, mandatory);
        line.setLineType(lineType);
        line.setFieldNames(Arrays.asList(fields));
        return line;
    }
}
