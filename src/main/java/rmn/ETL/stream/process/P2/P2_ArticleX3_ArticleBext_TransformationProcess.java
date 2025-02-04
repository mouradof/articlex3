package rmn.ETL.stream.process.P2;

import com.example.common_library.processes.P2_Common_TransformationProcess;
import rmn.ETL.stream.entities.ARTICLEX3;
import rmn.ETL.stream.entities.ARTICLEX3_BEXT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class P2_ArticleX3_ArticleBext_TransformationProcess
        extends P2_Common_TransformationProcess<ARTICLEX3, ARTICLEX3_BEXT> {

    public P2_ArticleX3_ArticleBext_TransformationProcess() {
        super(ARTICLEX3.class, ARTICLEX3_BEXT.class);
    }

    @Override
    public ARTICLEX3_BEXT transformToTargetEntity(ARTICLEX3 sourceEntity) {
        ARTICLEX3_BEXT targetEntity = new ARTICLEX3_BEXT();

        targetEntity.setReference(getTrimmedFieldValue(sourceEntity, "ITMREF"));
        targetEntity.setEan13(getTrimmedFieldValue(sourceEntity, "EANCOD"));
        targetEntity.setDesignation(getTrimmedFieldValue(sourceEntity, "DES1AXX"));
        targetEntity.setDebutVie(getTrimmedFieldValue(sourceEntity, "LIFSTRDAT"));
        targetEntity.setFinVie(getTrimmedFieldValue(sourceEntity, "LIFENDDAT"));
        targetEntity.setCodeVie(getTrimmedFieldValue(sourceEntity, "ITMSTA"));

        log.info("Transformed to ARTICLEX3_BEXT: {}", targetEntity);
        return targetEntity;
    }

    private String getTrimmedFieldValue(ARTICLEX3 entity, String fieldName) {
        try {
            return entity.getFirstLine("I").getFieldValue(fieldName).trim();
        } catch (Exception e) {
            log.warn("Field '{}' not found for line type '{}'. Error: {}", fieldName, "I", e.getMessage());
            return null;
        }
    }
}
