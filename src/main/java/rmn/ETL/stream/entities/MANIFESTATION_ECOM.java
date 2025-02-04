package rmn.ETL.stream.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MANIFESTATION_ECOM {
    private String codeManifestation;
    private String libelleManifestation;
    private String dateDebut;
    private String dateFin;
}
