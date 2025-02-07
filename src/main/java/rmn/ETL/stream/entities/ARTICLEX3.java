package rmn.ETL.stream.entities;

import com.example.common_library.utils.StructuredDataGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents an ARTICLEX3 entity that aggregates multiple structured data groups.
 * <p>
 * Each structured data group is associated with a specific line type.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ARTICLEX3 extends StructuredDataGroup {

    /**
     * List of structured data groups wrapped with their corresponding line types.
     */
    private List<StructuredDataGroupWrapper> structuredDataGroups = new ArrayList<>();

    /**
     * Retrieves the first structured data group matching the specified line type.
     *
     * @param lineType the type of line to search for.
     * @return the first matching structured data group.
     * @throws IllegalArgumentException if no structured data group is found for the given line type.
     */
    public StructuredDataGroup getFirstLine(String lineType) {
        return structuredDataGroups.stream()
                .filter(group -> group.getLineType().equals(lineType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No line found for type: " + lineType))
                .getDataGroup();
    }

    /**
     * Retrieves all structured data group wrappers matching the specified line type.
     *
     * @param lineType the type of line to filter by.
     * @return a list of matching structured data group wrappers.
     */
    public List<StructuredDataGroupWrapper> getLines(String lineType) {
        return structuredDataGroups.stream()
                .filter(group -> group.getLineType().equals(lineType))
                .collect(Collectors.toList());
    }

    /**
     * Adds a new structured data group with the specified line type.
     *
     * @param lineType  the type of the line.
     * @param dataGroup the structured data group to add.
     */
    public void addStructuredDataGroup(String lineType, StructuredDataGroup dataGroup) {
        this.structuredDataGroups.add(new StructuredDataGroupWrapper(lineType, dataGroup));
    }

    /**
     * Retrieves the last structured data group matching the specified line type.
     *
     * @param lineType the type of line to search for.
     * @return the last matching structured data group, or null if none is found.
     */
    public StructuredDataGroup getLastLine(String lineType) {
        for (int i = structuredDataGroups.size() - 1; i >= 0; i--) {
            if (structuredDataGroups.get(i).getLineType().equals(lineType)) {
                return structuredDataGroups.get(i).getDataGroup();
            }
        }
        return null;
    }

    /**
     * Wrapper class that associates a line type with a structured data group.
     */
    @Data
    @NoArgsConstructor
    public static class StructuredDataGroupWrapper {
        /**
         * The type identifier for the line.
         */
        private String lineType;
        /**
         * The structured data group corresponding to the line.
         */
        private StructuredDataGroup dataGroup;

        /**
         * Constructs a wrapper with the specified line type and structured data group.
         *
         * @param lineType  the type of the line.
         * @param dataGroup the structured data group.
         */
        public StructuredDataGroupWrapper(String lineType, StructuredDataGroup dataGroup) {
            this.lineType = lineType;
            this.dataGroup = dataGroup;
        }
    }
}
