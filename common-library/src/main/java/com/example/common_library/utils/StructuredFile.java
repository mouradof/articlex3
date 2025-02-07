package com.example.common_library.utils;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class StructuredFile {

    private List<StructuredLine> lineStructures = new ArrayList<>();

    // Par défaut, on met le séparateur de champs à "|"
    private String fieldSeparator = "|";

    // Autres propriétés par défaut
    private String EOL = "\n";
    private String targetFilename = "output";
    private String targetExtension = ".txt";
    private List<String> headerLines = new ArrayList<>();
    private List<String> footerLines = new ArrayList<>();

    /**
     * Recherche la structure de ligne par identifiant (ex : "I", "M", etc.)
     */
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
        private String identifier;  // ex. "I", "M", ...
        private String lineType;    // "I", "M", "ITRD", ...
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

        /**
         * Crée un StructuredDataGroup en associant chaque champ (à partir de fileLineFields[1]) aux noms de champs de fieldNames.
         */
        public StructuredDataGroup createStructuredDataGroup(String[] fileLineFields) {
            // On associe ce DataGroup à l'identifier (ex: "I")
            StructuredDataGroup dataGroup = new StructuredDataGroup(identifier);

            // On commence à i=1 si fileLineFields[0] = le type de ligne (I, M, etc.)
            for (int i = 1; i < fileLineFields.length; i++) {
                // Vérifie qu'on ne dépasse pas la liste de fieldNames
                if (i - 1 < fieldNames.size()) {
                    // Nom du champ configuré
                    String fieldName = fieldNames.get(i - 1);
                    // Valeur du champ
                    String fieldValue = fileLineFields[i];
                    // Ajout du champ à dataGroup
                    dataGroup.getFields().add(
                            new StructuredDataGroup.TranslatedField(
                                    fieldName,
                                    new StructuredDataGroup.TranslatedField.TranslatedValue("default", fieldValue)
                            )
                    );
                }
            }
            return dataGroup;
        }
    }
}
