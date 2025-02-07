package rmn.ETL.stream.process.P2;

import com.example.common_library.processes.P2_Common_TransformationProcess;
import rmn.ETL.stream.entities.ARTICLEX3;
import rmn.ETL.stream.entities.ARTICLEX3_ECOM;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Slf4j
@Service
public class P2_ArticleX3_ArticleEcom_TransformationProcess
        extends P2_Common_TransformationProcess<ARTICLEX3, ARTICLEX3_ECOM> {

    public P2_ArticleX3_ArticleEcom_TransformationProcess() {
        super(ARTICLEX3.class, ARTICLEX3_ECOM.class);
    }

    @Override
    protected Properties loadConfig() {
        Properties props = new Properties();
        String bootstrapServers = System.getenv("KAFKA_BROKER");
        if (bootstrapServers == null || bootstrapServers.isBlank()) {
            bootstrapServers = "kafka:9092";
        }
        props.put("bootstrap.servers", bootstrapServers);

        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");

        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        return props;
    }

    @Override
    public ARTICLEX3_ECOM transformToTargetEntity(ARTICLEX3 sourceEntity) {
        ARTICLEX3_ECOM targetEntity = new ARTICLEX3_ECOM();

        targetEntity.setReferenceRMN(getTrimmedFieldValue(sourceEntity, "ITMREF"));
        targetEntity.setEan(getTrimmedFieldValue(sourceEntity, "EANCOD"));
        targetEntity.setDesignation1(getTrimmedFieldValue(sourceEntity, "DES1AXX"));
        targetEntity.setMarqueEditeur(getTrimmedFieldValue(sourceEntity, "YMARQUE"));

        log.info("Transformed to ARTICLEX3_ECOM: {}", targetEntity);
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
