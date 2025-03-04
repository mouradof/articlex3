package rmn.ETL.stream.process.P2;

import com.example.common_library.processes.P2_Common_TransformationProcess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import rmn.ETL.stream.entities.ARTICLEX3;
import rmn.ETL.stream.entities.MANIFESTATION_ECOM;

import java.util.Properties;

/**
 * Transformation process that converts ARTICLEX3 entities to MANIFESTATION_ECOM entities.
 * <p>
 * Runs under the "P2" profile. Kafka configuration is handled in the superclass (loadConfig() is overridden).
 */
@Slf4j
@Service
@Profile("P2")
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
            targetEntity.setCodeManifestation(getFieldValue(sourceEntity, "MA", "YCODMAN"));
            targetEntity.setDateDebut(getFieldValue(sourceEntity, "MA", "YDATDEB"));
            targetEntity.setDateFin(getFieldValue(sourceEntity, "MA", "YDATFIN"));
        } catch (Exception e) {
            log.warn("Error transforming MANIFESTATION_ECOM entity: {}", e.getMessage());
        }

        log.info("Transformed to MANIFESTATION_ECOM: {}", targetEntity);
        return targetEntity;
    }

    private String getFieldValue(ARTICLEX3 entity, String lineType, String fieldName) {
        try {
            return entity.getFirstLine(lineType).getFieldValue(fieldName).trim();
        } catch (Exception e) {
            log.warn("Field '{}' not found for line type '{}'. Error: {}", fieldName, lineType, e.getMessage());
            return null;
        }
    }
}
