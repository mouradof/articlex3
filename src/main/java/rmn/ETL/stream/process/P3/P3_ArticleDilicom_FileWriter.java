package rmn.ETL.stream.process.P3;

import com.example.common_library.processes.P3_Common_LoadProcess;
import com.example.common_library.utils.StructuredFile;
import rmn.ETL.stream.entities.ARTICLE_DILICOM;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public abstract class P3_ArticleDilicom_FileWriter extends P3_Common_LoadProcess<ARTICLE_DILICOM> {

    public P3_ArticleDilicom_FileWriter() {
        super(ARTICLE_DILICOM.class);
    }

    protected String formatTargetEntity(ARTICLE_DILICOM articleDilicom, StructuredFile currentFileStructure) {
        try {
            StringBuilder fileContent = new StringBuilder();

            fileContent.append(getSafeValue(articleDilicom.getAction())).append(currentFileStructure.getFieldSeparator())
                    .append(getSafeValue(articleDilicom.getEan13())).append(currentFileStructure.getFieldSeparator())
                    .append(getSafeValue(articleDilicom.getGln13())).append(currentFileStructure.getEOL());

            log.info("Formatted ARTICLE_DILICOM entity: {}", fileContent);
            return fileContent.toString();
        } catch (Exception e) {
            log.error("Error formatting ARTICLE_DILICOM entity: {}", e.getMessage());
            return "";
        }
    }

    private String getSafeValue(String value) {
        return value != null ? value : "";
    }
}
