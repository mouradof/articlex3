package rmn.ETL.stream.process.P3;

import com.example.common_library.processes.P3_Common_LoadProcess;
import com.example.common_library.utils.StructuredFile;
import rmn.ETL.stream.entities.ARTICLEX3_UR;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public abstract class P3_ArticleUR_FileWriter extends P3_Common_LoadProcess<ARTICLEX3_UR> {

    public P3_ArticleUR_FileWriter() {
        super(ARTICLEX3_UR.class);
    }

    protected String formatTargetEntity(ARTICLEX3_UR articleUR, StructuredFile currentFileStructure) {
        ObjectMapper xmlMapper = new XmlMapper();
        String xmlContent;

        try {
            xmlContent = xmlMapper.writeValueAsString(articleUR);
        } catch (Exception e) {
            log.error("Error formatting ARTICLEX3_UR entity to XML: {}", e.getMessage());
            return "";
        }

        log.info("Formatted ARTICLEX3_UR entity as XML: {}", xmlContent);
        return xmlContent;
    }
}
