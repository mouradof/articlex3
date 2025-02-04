package rmn.ETL.stream.process.P3;

import com.example.common_library.processes.P3_Common_LoadProcess;
import com.example.common_library.utils.StructuredFile;
import rmn.ETL.stream.entities.MANIFESTATION_ECOM;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public abstract class P3_ManifestationEcom_FileWriter extends P3_Common_LoadProcess<MANIFESTATION_ECOM> {

    public P3_ManifestationEcom_FileWriter() {
        super(MANIFESTATION_ECOM.class);
    }

    protected String formatTargetEntity(MANIFESTATION_ECOM manifestationEcom, StructuredFile currentFileStructure) {
        if (manifestationEcom == null) {
            log.warn("MANIFESTATION_ECOM entity is null. Skipping formatting.");
            return "";
        }

        StringBuilder fileContent = new StringBuilder();

        try {
            String fieldSeparator = currentFileStructure.getFieldSeparator();
            String eol = currentFileStructure.getEOL();

            fileContent.append(manifestationEcom.getCodeManifestation()).append(fieldSeparator)
                    .append(manifestationEcom.getLibelleManifestation()).append(fieldSeparator)
                    .append(manifestationEcom.getDateDebut()).append(fieldSeparator)
                    .append(manifestationEcom.getDateFin()).append(eol);

            log.info("Formatted MANIFESTATION_ECOM entity: {}", fileContent);
        } catch (Exception e) {
            log.error("Error while formatting MANIFESTATION_ECOM entity: {}", e.getMessage());
            return "";
        }

        return fileContent.toString();
    }
}
