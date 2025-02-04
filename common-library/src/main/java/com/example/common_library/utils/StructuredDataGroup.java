package com.example.common_library.utils;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
public class StructuredDataGroup {

    private String groupName; // Le nom du groupe de données
    private List<TranslatedField> fields = new ArrayList<>(); // Liste des champs traduits associés à ce groupe

    public StructuredDataGroup(String groupName) {
        this.groupName = groupName;
    }

    /**
     * Ajoute un champ traduit à ce groupe de données.
     * Si un champ avec le même nom existe déjà, il met à jour sa valeur.
     *
     * @param fieldName Le nom du champ.
     * @param value     La valeur traduite du champ.
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
     * Récupère la valeur d'un champ à partir de son nom.
     *
     * @param fieldName Le nom du champ.
     * @return La valeur du champ.
     * @throws IllegalArgumentException Si le champ n'existe pas.
     */
    public String getFieldValue(String fieldName) {
        return fields.stream()
                .filter(f -> f.getFieldName().equals(fieldName))
                .map(TranslatedField::getValue)
                .map(TranslatedField.TranslatedValue::getContent)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Field " + fieldName + " does not exist in the group."));
    }

    /**
     * Récupère une valeur traduite spécifique à partir de son nom et de la langue.
     *
     * @param fieldName Le nom du champ.
     * @param language  La langue de la traduction.
     * @return Le contenu traduit.
     * @throws IllegalArgumentException Si le champ ou la traduction n'existe pas.
     */
    public String getTranslatedFieldValue(String fieldName, String language) {
        return fields.stream()
                .filter(f -> f.getFieldName().equals(fieldName))
                .map(TranslatedField::getValue)
                .filter(v -> v.getLanguage().equals(language))
                .map(TranslatedField.TranslatedValue::getContent)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Field " + fieldName + " with language " + language + " does not exist."));
    }

    /**
     * Vérifie si un champ existe dans le groupe.
     *
     * @param fieldName Le nom du champ.
     * @return true si le champ existe, false sinon.
     */
    public boolean hasField(String fieldName) {
        return fields.stream().anyMatch(f -> f.getFieldName().equals(fieldName));
    }

    /**
     * Classe interne représentant un champ traduit.
     */
    @Data
    @NoArgsConstructor
    public static class TranslatedField {
        private String fieldName; // Nom du champ traduit
        private TranslatedValue value; // La valeur traduite du champ

        public TranslatedField(String fieldName, TranslatedValue value) {
            this.fieldName = fieldName;
            this.value = value;
        }

        /**
         * Classe interne représentant la valeur traduite d'un champ.
         */
        @Data
        @NoArgsConstructor
        public static class TranslatedValue {
            private String language; // La langue de la traduction
            private String content; // Le contenu traduit du champ

            public TranslatedValue(String language, String content) {
                this.language = language;
                this.content = content;
            }
        }
    }
}
