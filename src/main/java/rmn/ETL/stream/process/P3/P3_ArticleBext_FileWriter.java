package rmn.ETL.stream.process.P3;

import com.example.common_library.processes.P3_Common_LoadProcess;
import com.example.common_library.utils.StructuredFile;
import rmn.ETL.stream.entities.ARTICLEX3_BEXT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public abstract class P3_ArticleBext_FileWriter extends P3_Common_LoadProcess<ARTICLEX3_BEXT> {

    public P3_ArticleBext_FileWriter() {
        super(ARTICLEX3_BEXT.class);
    }

    protected String formatTargetEntity(ARTICLEX3_BEXT articleBext, StructuredFile currentFileStructure) {
        try {
            StringBuilder fileContent = new StringBuilder();

            fileContent.append(getSafeValue(articleBext.getReference())).append(currentFileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getEan13())).append(currentFileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getCodeRMN())).append(currentFileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getDesignation())).append(currentFileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getLibEtiquette())).append(currentFileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getCodeVie())).append(currentFileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getDebutVie())).append(currentFileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getFinVie())).append(currentFileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getCleGL())).append(currentFileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getLibCleGL())).append(currentFileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getCodeFournisseur())).append(currentFileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getNomFournisseur())).append(currentFileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getPoidsERP())).append(currentFileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getCodeDouane())).append(currentFileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getCategorieArticle())).append(currentFileStructure.getEOL());

            log.info("Formatted ARTICLEX3_BEXT entity: {}", fileContent);
            return fileContent.toString();
        } catch (Exception e) {
            log.error("Error formatting ARTICLEX3_BEXT entity: {}", e.getMessage());
            return "";
        }
    }

    private String getSafeValue(String value) {
        return value != null ? value : "";
    }
}
