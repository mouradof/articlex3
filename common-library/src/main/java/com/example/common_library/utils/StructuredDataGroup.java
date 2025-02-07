package com.example.common_library.utils;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
public class StructuredDataGroup {

    private String groupName; // ex: "I", "M", ...
    private List<TranslatedField> fields = new ArrayList<>();

    public StructuredDataGroup(String groupName) {
        this.groupName = groupName;
    }

    /**
     * Ajoute ou met à jour un champ (fieldName) avec la valeur (TranslatedValue).
     */
    public void addField(String fieldName, TranslatedField.TranslatedValue value) {
        Optional<TranslatedField> existingField = fields.stream()
                .filter(f -> f.getFieldName().equals(fieldName))
                .findFirst();

        if (existingField.isPresent()) {
            existingField.get().setValue(value);
        } else {
            fields.add(new TranslatedField(fieldName, value));
        }
    }

    /**
     * Récupère la valeur (content) d'un champ
     */
    public String getFieldValue(String fieldName) {
        return fields.stream()
                .filter(f -> f.getFieldName().equals(fieldName))
                .map(TranslatedField::getValue)
                .map(TranslatedField.TranslatedValue::getContent)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Field " + fieldName + " not found."));
    }

    /**
     * Vérifie si un champ existe
     */
    public boolean hasField(String fieldName) {
        return fields.stream().anyMatch(f -> f.getFieldName().equals(fieldName));
    }

    @Data
    @NoArgsConstructor
    public static class TranslatedField {
        private String fieldName; // Nom du champ
        private TranslatedValue value; // Valeur (avec langue et contenu)

        public TranslatedField(String fieldName, TranslatedValue value) {
            this.fieldName = fieldName;
            this.value = value;
        }

        @Data
        @NoArgsConstructor
        public static class TranslatedValue {
            private String language;
            private String content;

            public TranslatedValue(String language, String content) {
                this.language = language;
                this.content = content;
            }
        }
    }
}
