package rmn.ETL.stream.process.P0;

import com.example.common_library.processes.P0_FileReader;
import com.example.common_library.utils.StructuredDataGroup;
import com.example.common_library.utils.StructuredFile;
import com.example.common_library.utils.TopicNames;
import org.springframework.context.annotation.Profile;
import rmn.ETL.stream.entities.ARTICLEX3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;

@Slf4j
@Service
@Profile("P0")
public class P0_ArticleX3_FileReader extends P0_FileReader<ARTICLEX3> {

    @Autowired
    public P0_ArticleX3_FileReader(StructuredFile structureDescription, TopicNames<ARTICLEX3> topicNames,
                                   @Value("${KAFKA_BROKER:kafka:9092}") String kafkaBroker,
                                   @Value("${KAFKA_TOPIC_STAGING:staging_topic}") String stagingTopicName) {
        super(structureDescription, ARTICLEX3.class, topicNames, kafkaBroker, stagingTopicName);
    }

    public void processFile(File file) {
        log.info("Traitement du fichier : {}", file.getAbsolutePath());
        try {
            if (!file.exists()) {
                log.error("Le fichier {} n'existe pas !", file.getAbsolutePath());
                return;
            }
            super.processFile(file);
        } catch (Exception e) {
            log.error("Erreur lors du traitement du fichier {} :", file.getAbsolutePath(), e);
        }
    }

    @Override
    protected void createEntitySource(ARTICLEX3 articleX3Entity, String[] fileLineFields, StructuredFile.StructuredLine lineStructure) {
        log.debug("lineType={} fields={} (count={})",
                lineStructure.getLineType(),
                Arrays.toString(fileLineFields),
                fileLineFields.length);

        switch (lineStructure.getLineType()) {
            case "I":
                if (fileLineFields.length < 18) {
                    log.error("Ligne I invalide : {} colonnes -> {}", fileLineFields.length, Arrays.toString(fileLineFields));
                    return;
                }
                articleX3Entity.addStructuredDataGroup(
                        lineStructure.getLineType(),
                        lineStructure.createStructuredDataGroup(fileLineFields)
                );
                break;

            case "M":
                if (fileLineFields.length < 7) {
                    log.error("Ligne M invalide : {} colonnes -> {}", fileLineFields.length, Arrays.toString(fileLineFields));
                    return;
                }
                articleX3Entity.addStructuredDataGroup(
                        lineStructure.getLineType(),
                        lineStructure.createStructuredDataGroup(fileLineFields)
                );
                break;

            case "ITRD":
                if (fileLineFields.length < 4) {
                    log.error("Ligne ITRD invalide : {} colonnes -> {}", fileLineFields.length, Arrays.toString(fileLineFields));
                    return;
                }
                if (articleX3Entity.getLastLine("I") != null) {
                    articleX3Entity.getLastLine("I")
                            .addField(
                                    fileLineFields[1],
                                    new StructuredDataGroup.TranslatedField.TranslatedValue(fileLineFields[2], fileLineFields[3])
                            );
                } else {
                    log.warn("Aucune ligne 'I' trouvée pour ITRD");
                }
                break;

            case "MTRD":
                if (fileLineFields.length < 4) {
                    log.error("Ligne MTRD invalide : {} colonnes -> {}", fileLineFields.length, Arrays.toString(fileLineFields));
                    return;
                }
                if (articleX3Entity.getLastLine("M") != null) {
                    articleX3Entity.getLastLine("M")
                            .addField(
                                    fileLineFields[1],
                                    new StructuredDataGroup.TranslatedField.TranslatedValue(fileLineFields[2], fileLineFields[3])
                            );
                } else {
                    log.warn("Aucune ligne 'M' trouvée pour MTRD");
                }
                break;

            default:
                articleX3Entity.addStructuredDataGroup(
                        lineStructure.getLineType(),
                        lineStructure.createStructuredDataGroup(fileLineFields)
                );
                log.warn("Type de ligne inconnu: {}", lineStructure.getLineType());
        }
    }
}
