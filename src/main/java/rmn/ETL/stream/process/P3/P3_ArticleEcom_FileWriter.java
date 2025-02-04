package rmn.ETL.stream.process.P3;

import com.example.common_library.processes.P3_Common_LoadProcess;
import com.example.common_library.utils.StructuredFile;
import rmn.ETL.stream.entities.ARTICLEX3_ECOM;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public abstract class P3_ArticleEcom_FileWriter extends P3_Common_LoadProcess<ARTICLEX3_ECOM> {

    public P3_ArticleEcom_FileWriter() {
        super(ARTICLEX3_ECOM.class);
    }

    protected String formatTargetEntity(ARTICLEX3_ECOM articleEcom, StructuredFile currentFileStructure) {
        try {
            StringBuilder fileContent = new StringBuilder();

            fileContent.append(getSafeValue(articleEcom.getReferenceRMN())).append(currentFileStructure.getFieldSeparator())
                    .append(getSafeValue(articleEcom.getEan())).append(currentFileStructure.getFieldSeparator())
                    .append(getSafeValue(articleEcom.getDesignation1())).append(currentFileStructure.getFieldSeparator())
                    .append(getSafeValue(articleEcom.getMarqueEditeur())).append(currentFileStructure.getFieldSeparator())
                    .append(getSafeValue(articleEcom.getStatutX3())).append(currentFileStructure.getFieldSeparator())
                    .append(getSafeValue(articleEcom.getPoidsEmballe())).append(currentFileStructure.getFieldSeparator())
                    .append(getSafeValue(articleEcom.getLibelleLigneDediee())).append(currentFileStructure.getFieldSeparator())
                    .append(String.join(";", articleEcom.getCodesManifestation())).append(currentFileStructure.getFieldSeparator())
                    .append(getSafeValue(articleEcom.getTechniqueImpression())).append(currentFileStructure.getEOL());

            log.info("Formatted ARTICLEX3_ECOM entity: {}", fileContent);
            return fileContent.toString();
        } catch (Exception e) {
            log.error("Error formatting ARTICLEX3_ECOM entity: {}", e.getMessage());
            return "";
        }
    }

    private String getSafeValue(String value) {
        return value != null ? value : "";
    }
}
