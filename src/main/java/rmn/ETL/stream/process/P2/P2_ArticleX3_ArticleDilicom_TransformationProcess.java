package rmn.ETL.stream.process.P2;

import com.example.common_library.processes.P2_Common_TransformationProcess;
import rmn.ETL.stream.entities.ARTICLEX3;
import rmn.ETL.stream.entities.ARTICLE_DILICOM;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class P2_ArticleX3_ArticleDilicom_TransformationProcess
        extends P2_Common_TransformationProcess<ARTICLEX3, ARTICLE_DILICOM> {

    public P2_ArticleX3_ArticleDilicom_TransformationProcess() {
        super(ARTICLEX3.class, ARTICLE_DILICOM.class);
    }

    @Override
    public ARTICLE_DILICOM transformToTargetEntity(ARTICLEX3 sourceEntity) {
        ARTICLE_DILICOM targetEntity = new ARTICLE_DILICOM();

        targetEntity.setAction("C");
        targetEntity.setEan13(getTrimmedFieldValue(sourceEntity, "I", "EANCOD"));
        targetEntity.setGln13(getTrimmedFieldValue(sourceEntity, "P", "GLNCOD"));

        log.info("Transformed to ARTICLE_DILICOM: {}", targetEntity);
        return targetEntity;
    }

    private String getTrimmedFieldValue(ARTICLEX3 entity, String lineType, String fieldName) {
        try {
            return entity.getFirstLine(lineType).getFieldValue(fieldName).trim();
        } catch (Exception e) {
            log.warn("Field '{}' not found for line type '{}'. Error: {}", fieldName, lineType, e.getMessage());
            return null;
        }
    }
}
