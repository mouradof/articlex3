package rmn.ETL.stream.entities;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ARTICLE_DILICOM {
    private String action;
    private String ean13;
    private String gln13;

    public String getAction() { return action; }
    public String getEan13() { return ean13; }
    public String getGln13() { return gln13; }
}
