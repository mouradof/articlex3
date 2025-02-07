package rmn.ETL.stream.service;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import rmn.ETL.stream.process.P0.P0_ArticleX3_FileReader;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for polling the input directory and processing files.
 * <p>
 * This service runs under the "P0" profile and checks the configured input directory every 5 seconds.
 * Detected files are processed using the P0_ArticleX3_FileReader and then deleted.
 */
@Slf4j
@Service
@Profile({"P0"})
public class FilePollerService {

    @Value("${INPUT_DIR}")
    private String inputDir;

    private final P0_ArticleX3_FileReader fileReader;

    /**
     * Constructor that injects the file reader dependency.
     *
     * @param fileReader the service responsible for processing Article X3 files.
     */
    public FilePollerService(P0_ArticleX3_FileReader fileReader) {
        this.fileReader = fileReader;
    }

    /**
     * Polls the input directory at fixed intervals for new files.
     * <p>
     * For each file found, the file is processed and, if successful, deleted.
     */
    @Scheduled(fixedDelayString = "5000")
    public void pollForFiles() {
        File directory = new File(inputDir);
        if (!directory.exists() || !directory.isDirectory()) {
            log.error("Directory {} does not exist or is not a directory", inputDir);
            return;
        }
        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            log.info("No files to process in directory {}", inputDir);
            return;
        }
        for (File file : files) {
            if (file.isFile()) {
                log.info("File detected: {}", file.getAbsolutePath());
                try {
                    fileReader.processFile(file);
                    if (file.delete()) {
                        log.info("File {} deleted after processing.", file.getName());
                    } else {
                        log.warn("Unable to delete file {}.", file.getName());
                    }
                } catch (Exception e) {
                    log.error("Error processing file {}", file.getAbsolutePath(), e);
                }
            }
        }
    }
}
