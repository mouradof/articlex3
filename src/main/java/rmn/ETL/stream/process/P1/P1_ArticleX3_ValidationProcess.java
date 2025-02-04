package rmn.ETL.stream.process.P1;

import com.example.common_library.processes.P1_Common_ValidationProcess;
import com.example.common_library.utils.TopicNames;
import rmn.ETL.stream.entities.ARTICLEX3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class P1_ArticleX3_ValidationProcess extends P1_Common_ValidationProcess<ARTICLEX3> implements CommandLineRunner {

    @Autowired
    public P1_ArticleX3_ValidationProcess(TopicNames<ARTICLEX3> topicNames) {
        super(ARTICLEX3.class);
        this.topicNames = topicNames;
    }

    @Override
    public List<String> validateEntitySource(ARTICLEX3 entity) {
        // Votre logique de validation existante
        List<String> errors = new ArrayList<>();

        if (entity.getStructuredDataGroups().isEmpty()) {
            errors.add("No data found in ARTICLEX3 entity.");
        }

        entity.getLines("I").forEach(line -> {
            String reference = line.getDataGroup().getFieldValue("ITMREF");
            if (reference == null || reference.isEmpty()) {
                errors.add("Missing 'ITMREF' field in line type 'I'.");
            }
        });

        List<String> itemReferences = entity.getLines("I").stream()
                .map(line -> line.getDataGroup().getFieldValue("ITMREF"))
                .toList();
        if (itemReferences.size() != itemReferences.stream().distinct().count()) {
            errors.add("Duplicate 'ITMREF' values found in line type 'I'.");
        }

        log.debug("Validation completed with {} errors.", errors.size());
        return errors;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting P1_ArticleX3_ValidationProcess...");
        runProcess(); // Appelle la méthode de consommation et validation Kafka
    }
}
