package rmn.ETL.stream.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an e-commerce manifestation entity.
 * <p>
 * This entity stores details about a manifestation including its unique code,
 * descriptive label, start date, and end date.
 */
@Data
@NoArgsConstructor
public class MANIFESTATION_ECOM {
    private String codeManifestation;
    private String libelleManifestation;
    private String dateDebut;
    private String dateFin;
}
