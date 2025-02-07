package rmn.ETL.stream.process.P3;

import com.example.common_library.processes.P3_Common_LoadProcess;
import com.example.common_library.utils.StructuredFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import rmn.ETL.stream.entities.ARTICLEX3_BEXT;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

@Slf4j
@Service
public class P3_ArticleBext_FileWriter extends P3_Common_LoadProcess<ARTICLEX3_BEXT> implements CommandLineRunner {

    // Dossier de sortie (utilisé pour construire le chemin complet du fichier de sortie)
    private final String outputDirectory;
    // Nom fixe du fichier de sortie
    private final String outputFileName = "bext_final_output.txt";
    // Structure du fichier (séparateur, EOL, …)
    private final StructuredFile currentFileStructure;

    // Instance pour la désérialisation (exemple avec Jackson)
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Constructeur avec injection de la structure du fichier.
     * On lit ici la variable d'environnement OUTPUT_DIRECTORY.
     */
    public P3_ArticleBext_FileWriter(StructuredFile currentFileStructure) {
        super(ARTICLEX3_BEXT.class);
        this.currentFileStructure = currentFileStructure;
        String envOutputDir = System.getenv("OUTPUT_DIRECTORY");
        if (envOutputDir == null || envOutputDir.isBlank()) {
            this.outputDirectory = "/app/output";
        } else {
            this.outputDirectory = envOutputDir;
        }
        log.info("P3_ArticleBext_FileWriter initialized with outputDirectory: {}", this.outputDirectory);
    }

    /**
     * Configuration du KafkaConsumer pour P3.
     * Ajout d'un group.id spécifique pour éviter les conflits avec d'autres consommateurs.
     */
    @Override
    protected Properties loadConfig() {
        Properties props = new Properties();
        String bootstrapServers = System.getenv("KAFKA_BROKER");
        if (bootstrapServers == null || bootstrapServers.isBlank()) {
            bootstrapServers = "localhost:9092";
        }
        props.put("bootstrap.servers", bootstrapServers);
        // Définition d'un identifiant de groupe spécifique pour ce consommateur
        props.put("group.id", "p3-articlebext-filewriter");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
        log.info("loadConfig() set bootstrap.servers to {}", bootstrapServers);
        return props;
    }

    /**
     * Formate l'entité ARTICLEX3_BEXT en une chaîne de caractères en se basant sur la structure définie.
     */
    protected String formatTargetEntity(ARTICLEX3_BEXT articleBext, StructuredFile fileStructure) {
        try {
            StringBuilder fileContent = new StringBuilder();

            fileContent.append(getSafeValue(articleBext.getReference())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getEan13())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getCodeRMN())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getDesignation())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getLibEtiquette())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getCodeVie())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getDebutVie())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getFinVie())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getCleGL())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getLibCleGL())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getCodeFournisseur())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getNomFournisseur())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getPoidsERP())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getCodeDouane())).append(fileStructure.getFieldSeparator())
                    .append(getSafeValue(articleBext.getCategorieArticle())).append(fileStructure.getEOL());

            log.info("Formatted ARTICLEX3_BEXT entity: {}", fileContent);
            return fileContent.toString();
        } catch (Exception e) {
            log.error("Error formatting ARTICLEX3_BEXT entity: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Méthode utilitaire pour éviter les valeurs nulles.
     */
    private String getSafeValue(String value) {
        return value != null ? value : "";
    }

    /**
     * Méthode chargée d'écrire l'entité transformée dans le fichier de sortie.
     */
    @Override
    public void loadTargetEntity(ARTICLEX3_BEXT targetEntity) {
        log.info("loadTargetEntity() called with entity: {}", targetEntity);
        String formattedLine = formatTargetEntity(targetEntity, currentFileStructure);
        // Construit le chemin complet du fichier de sortie
        String fullOutputFilePath = outputDirectory + "/" + outputFileName;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fullOutputFilePath, true))) {
            writer.write(formattedLine);
            writer.newLine();
            writer.flush();
            log.info("Entity loaded successfully and written to file: {}", targetEntity.getReference());
        } catch (IOException e) {
            log.error("Error writing entity to file: {}", e.getMessage());
        }
    }

    /**
     * Méthode de débogage appelée après l'initialisation du bean.
     */
    @PostConstruct
    public void debugInitialization() {
        String fullPath = outputDirectory + "/" + outputFileName;
        log.info("P3_ArticleBext_FileWriter started with outputDirectory: {} and file name: {}",
                outputDirectory, outputFileName);
        log.info("Full output file path will be: {}", fullPath);
    }

    /**
     * La méthode run() déclenche la consommation des messages Kafka dès le démarrage de l'application.
     */
    @Override
    public void run(String... args) {
        log.info("Starting Kafka consumer for P3_ArticleBext_FileWriter...");
        runProcess();
    }

    /**
     * Implémente le processus de consommation Kafka et l'écriture dans le fichier.
     */
    private void runProcess() {
        // Chargement de la configuration Kafka
        Properties props = loadConfig();
        // Récupération du nom du topic depuis l'environnement ou utilisation d'une valeur par défaut
        String topic = System.getenv("KAFKA_TOPIC_TRANSFORMED");
        if (topic == null || topic.isBlank()) {
            topic = "article_bext_transformed";
        }
        // Création du consumer Kafka
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));
            log.info("Subscribed to Kafka topic: {}", topic);
            // Boucle de consommation
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    log.info("Received message: key={}, value={}", record.key(), record.value());
                    // Conversion du message en entité ARTICLEX3_BEXT
                    ARTICLEX3_BEXT entity = convertRecordToEntity(record.value());
                    if (entity != null) {
                        loadTargetEntity(entity);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error in Kafka consumer loop: {}", e.getMessage());
        }
    }

    /**
     * Méthode de conversion du contenu du message en objet ARTICLEX3_BEXT.
     * Ici, on suppose que le message est au format JSON.
     */
    private ARTICLEX3_BEXT convertRecordToEntity(String recordValue) {
        try {
            return objectMapper.readValue(recordValue, ARTICLEX3_BEXT.class);
        } catch (Exception e) {
            log.error("Error converting record to ARTICLEX3_BEXT: {}", e.getMessage());
            return null;
        }
    }
}
