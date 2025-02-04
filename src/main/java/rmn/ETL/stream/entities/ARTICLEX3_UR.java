package rmn.ETL.stream.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    @Data
    @NoArgsConstructor
    public static class LotType {
        private String lotTypeCode;
        private boolean remove;
    }

    @Data
    @NoArgsConstructor
    public static class Item {
        private String code;
        private Barcodes barcodes;

        @Data
        @NoArgsConstructor
        public static class Barcodes {
            private PrimaryBarcode primaryBarcode;
            private SecondaryBarcode secondaryBarcode;

            @Data
            @NoArgsConstructor
            public static class PrimaryBarcode {
                private String barcodeValue;
                private boolean remove;
            }

            @Data
            @NoArgsConstructor
            public static class SecondaryBarcode {
                private String type;
                private String barcodeValue;
                private boolean remove;
            }
        }
    }

    @Data
    @NoArgsConstructor
    public static class Hierarchy {
        private String hierarchyLevel;
    }

    @Data
    @NoArgsConstructor
    public static class Attribute {
        private String attributeCode;
        private String attributeValue;
        private boolean remove;
    }

    @Data
    @NoArgsConstructor
    public static class Vat {
        private String vatCode;
        private String countryCode;
        private boolean remove;
    }
}
