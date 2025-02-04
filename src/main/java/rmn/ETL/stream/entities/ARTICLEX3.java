package rmn.ETL.stream.entities;

import com.example.common_library.utils.StructuredDataGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ARTICLEX3 extends StructuredDataGroup {

    private List<StructuredDataGroupWrapper> structuredDataGroups = new ArrayList<>();


    public StructuredDataGroup getFirstLine(String lineType) {
        return structuredDataGroups.stream()
                .filter(group -> group.getLineType().equals(lineType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No line found for type: " + lineType)).getDataGroup();
    }

    public List<StructuredDataGroupWrapper> getLines(String lineType) {
        return structuredDataGroups.stream()
                .filter(group -> group.getLineType().equals(lineType))
                .collect(Collectors.toList());
    }

    public void addStructuredDataGroup(String lineType, StructuredDataGroup dataGroup) {
        this.structuredDataGroups.add(new StructuredDataGroupWrapper(lineType, dataGroup));
    }

    public StructuredDataGroup getLastLine(String lineType) {
        for (int i = structuredDataGroups.size() - 1; i >= 0; i--) {
            if (structuredDataGroups.get(i).getLineType().equals(lineType)) {
                return structuredDataGroups.get(i).getDataGroup();
            }
        }
        return null;
    }

    @Data
    @NoArgsConstructor
    public static class StructuredDataGroupWrapper {
        private String lineType;
        private StructuredDataGroup dataGroup;

        public StructuredDataGroupWrapper(String lineType, StructuredDataGroup dataGroup) {
            this.lineType = lineType;
            this.dataGroup = dataGroup;
        }

        public String getLineType() {
            return lineType;
        }

        public StructuredDataGroup getDataGroup() {
            return dataGroup;
        }
    }
}
