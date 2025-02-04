package com.example.common_library.utils;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class StructuredFile {
    private List<StructuredLine> lineStructures = new ArrayList<>();
    // Valeur par défaut définie pour fieldSeparator (par exemple, une virgule)
    private String fieldSeparator = ",";
    // Valeurs par défaut pour d'autres propriétés (à adapter selon vos besoins)
    private String EOL = "\n";
    private String targetFilename = "output";
    private String targetExtension = ".txt";
    private List<String> headerLines = new ArrayList<>();
    private List<String> footerLines = new ArrayList<>();

    public StructuredLine getLineStructure(String identifier) {
        for (StructuredLine lineStructure : lineStructures) {
            if (lineStructure.getIdentifier().equals(identifier)) {
                return lineStructure;
            }
        }
        return null;
    }

    public void addLineStructure(StructuredLine lineStructure) {
        this.lineStructures.add(lineStructure);
    }

    @Data
    @NoArgsConstructor
    public static class StructuredLine {
        private String identifier;
        private String lineType;
        private boolean isHeader;
        private List<String> fieldNames = new ArrayList<>();

        public StructuredLine(String identifier, boolean isHeader) {
            this.identifier = identifier;
            this.isHeader = isHeader;
        }

        public StructuredLine(String identifier, String lineType) {
            this(identifier, true);
            this.lineType = lineType;
        }

        public StructuredDataGroup createStructuredDataGroup(String[] fileLineFields) {
            StructuredDataGroup dataGroup = new StructuredDataGroup(identifier);
            for (int i = 1; i < fileLineFields.length; i++) {
                dataGroup.getFields().add(
                        new StructuredDataGroup.TranslatedField(
                                this.fieldNames.get(i - 1),
                                new StructuredDataGroup.TranslatedField.TranslatedValue("default", fileLineFields[i])
                        )
                );
            }
            return dataGroup;
        }
    }
}
