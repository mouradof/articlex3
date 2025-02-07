package rmn.ETL.stream.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a DILICOM article entity.
 * <p>
 * This entity stores basic information such as the action to be performed,
 * the EAN-13 product code, and the GLN-13 location code.
 */
@Data
@NoArgsConstructor
public class ARTICLE_DILICOM {
    private String action;
    private String ean13;
    private String gln13;
}
