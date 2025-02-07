package rmn.ETL.stream.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the UR details for an ARTICLEX3 entity.
 * <p>
 * This entity encapsulates identification codes, descriptive information, a sales unit,
 * and nested lists for items, lot types, hierarchies, attributes, and VAT details.
 */
@Data
@NoArgsConstructor
public class ARTICLEX3_UR {
    private String code;
    private String state;
    private String shortDescription;
    private String description;
    private String pattern;
    private String salesUnit;
    private List<Item> items = new ArrayList<>();
    private List<LotType> lotTypes = new ArrayList<>();
    private List<Hierarchy> hierarchies = new ArrayList<>();
    private List<Attribute> attributes = new ArrayList<>();
    private List<Vat> vats;
    private String rmnEntityId;

    /**
     * Represents a lot type associated with the article.
     */
    @Data
    @NoArgsConstructor
    public static class LotType {
        private String lotTypeCode;
        private boolean remove;
    }

    /**
     * Represents an item, including its code and related barcode information.
     */
    @Data
    @NoArgsConstructor
    public static class Item {
        private String code;
        private Barcodes barcodes;

        /**
         * Encapsulates both primary and secondary barcode details.
         */
        @Data
        @NoArgsConstructor
        public static class Barcodes {
            private PrimaryBarcode primaryBarcode;
            private SecondaryBarcode secondaryBarcode;

            /**
             * Represents the primary barcode details.
             */
            @Data
            @NoArgsConstructor
            public static class PrimaryBarcode {
                private String barcodeValue;
                private boolean remove;
            }

            /**
             * Represents the secondary barcode details.
             */
            @Data
            @NoArgsConstructor
            public static class SecondaryBarcode {
                private String type;
                private String barcodeValue;
                private boolean remove;
            }
        }
    }

    /**
     * Represents a hierarchy level detail.
     */
    @Data
    @NoArgsConstructor
    public static class Hierarchy {
        private String hierarchyLevel;
    }

    /**
     * Represents an attribute associated with the article.
     */
    @Data
    @NoArgsConstructor
    public static class Attribute {
        private String attributeCode;
        private String attributeValue;
        private boolean remove;
    }

    /**
     * Represents VAT information including VAT and country codes.
     */
    @Data
    @NoArgsConstructor
    public static class Vat {
        private String vatCode;
        private String countryCode;
        private boolean remove;
    }
}
