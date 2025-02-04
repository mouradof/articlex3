package rmn.ETL.stream.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ARTICLEX3_BEXT {
    private String reference;
    private String ean13;
    private String codeRMN;
    private String designation;
    private String libEtiquette;
    private String codeVie;
    private String debutVie;
    private String finVie;
    private String cleGL;
    private String libCleGL;
    private String codeFournisseur;
    private String nomFournisseur;
    private String poidsERP;
    private String codeDouane;
    private String categorieArticle;
}
