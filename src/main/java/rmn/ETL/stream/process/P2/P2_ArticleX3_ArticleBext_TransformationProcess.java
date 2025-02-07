package rmn.ETL.stream.process.P2;

import com.example.common_library.processes.P2_Common_TransformationProcess;
import com.example.common_library.utils.TopicNames;
import rmn.ETL.stream.entities.ARTICLEX3;
import rmn.ETL.stream.entities.ARTICLEX3_BEXT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Slf4j
@Service
@Profile("P2")
public class P2_ArticleX3_ArticleBext_TransformationProcess
        extends P2_Common_TransformationProcess<ARTICLEX3, ARTICLEX3_BEXT>
        implements CommandLineRunner {

    @Autowired
    public P2_ArticleX3_ArticleBext_TransformationProcess(TopicNames<ARTICLEX3_BEXT> topicNames) {
        super(ARTICLEX3.class, ARTICLEX3_BEXT.class);
        this.topicNames = topicNames;
    }

    @Override
    public void run(String... args) {
        log.info("🚀 Démarrage du process P2_ArticleX3_ArticleBext_TransformationProcess...");
        run();
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

        log.info("🛠️ Kafka Properties : {}", props);
        return props;
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

        log.info("🔄 Transformation effectuée : {}", targetEntity);
        return targetEntity;
    }

    private String getTrimmedFieldValue(ARTICLEX3 entity, String fieldName) {
        try {
            return entity.getFirstLine("I").getFieldValue(fieldName).trim();
        } catch (Exception e) {
            log.warn("⚠️ Champ '{}' introuvable pour la ligne 'I'. Erreur: {}", fieldName, e.getMessage());
            return null;
        }
    }
}
