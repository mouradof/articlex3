package rmn.ETL.stream.process.P2;

import com.example.common_library.processes.P2_Common_TransformationProcess;
import rmn.ETL.stream.entities.ARTICLEX3;
import rmn.ETL.stream.entities.MANIFESTATION_ECOM;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Slf4j
@Service
public class P2_ArticleX3_ManifestationEcom_TransformationProcess
        extends P2_Common_TransformationProcess<ARTICLEX3, MANIFESTATION_ECOM> {

    public P2_ArticleX3_ManifestationEcom_TransformationProcess() {
        super(ARTICLEX3.class, MANIFESTATION_ECOM.class);
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
    public MANIFESTATION_ECOM transformToTargetEntity(ARTICLEX3 sourceEntity) {
        MANIFESTATION_ECOM targetEntity = new MANIFESTATION_ECOM();

        try {
            targetEntity.setCodeManifestation(getFieldValue(sourceEntity, "YCODMAN"));
            targetEntity.setDateDebut(getFieldValue(sourceEntity, "YDATDEB"));
            targetEntity.setDateFin(getFieldValue(sourceEntity, "YDATFIN"));
        } catch (Exception e) {
            log.warn("Error transforming MANIFESTATION_ECOM entity: {}", e.getMessage());
        }

        log.info("Transformed to MANIFESTATION_ECOM: {}", targetEntity);
        return targetEntity;
    }

    private String getFieldValue(ARTICLEX3 entity, String fieldName) {
        try {
            return entity.getFirstLine("MA").getFieldValue(fieldName).trim();
        } catch (Exception e) {
            log.warn("Field '{}' not found for line type '{}'. Error: {}", fieldName, "MA", e.getMessage());
            return null;
        }
    }
}
