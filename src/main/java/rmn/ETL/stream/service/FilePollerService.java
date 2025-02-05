package rmn.ETL.stream.service;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import rmn.ETL.stream.process.P0.P0_ArticleX3_FileReader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile({"P0"})
public class FilePollerService {

    @Value("${INPUT_DIR}")
    private String inputDir;

    private final P0_ArticleX3_FileReader fileReader;

    public FilePollerService(P0_ArticleX3_FileReader fileReader) {
        this.fileReader = fileReader;
    }

    @Scheduled(fixedDelayString = "5000")
    public void pollForFiles() {
        File directory = new File(inputDir);
        if (!directory.exists() || !directory.isDirectory()) {
            log.error("Le dossier {} n'existe pas ou n'est pas un dossier", inputDir);
            return;
        }
        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            log.info("Aucun fichier à traiter dans le dossier {}", inputDir);
            return;
        }
        for (File file : files) {
            if (file.isFile()) {
                log.info("Fichier détecté : {}", file.getAbsolutePath());
                try {
                    fileReader.processFile(file);
                    if (file.delete()) {
                        log.info("Fichier {} supprimé après traitement.", file.getName());
                    } else {
                        log.warn("Impossible de supprimer le fichier {}.", file.getName());
                    }
                } catch (Exception e) {
                    log.error("Erreur lors du traitement du fichier {}", file.getAbsolutePath(), e);
                }
            }
        }
    }
}
