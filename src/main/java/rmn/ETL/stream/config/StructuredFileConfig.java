package rmn.ETL.stream.config;

import com.example.common_library.utils.StructuredFile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Arrays;

@Configuration
public class StructuredFileConfig {

    @Bean
    public StructuredFile articleX3FileStructure() {
        StructuredFile fileStructure = new StructuredFile();

        StructuredFile.StructuredLine lineI = new StructuredFile.StructuredLine("I", true);
        lineI.setLineType("I");
        lineI.setFieldNames(Arrays.asList(
                "TCLCOD","YCLEALCE","ITMREF","EANCOD","DES1AXX","DES2AXX","DES3AXX","ITMSTA","YDISPCDE","LIFSTRDAT",
                "LIFENDDAT","YPARDAT","YUNITE","STU","WEU","ITMWEI","YPOIDSNU"
        ));
        fileStructure.addLineStructure(lineI);

        StructuredFile.StructuredLine lineITRD = new StructuredFile.StructuredLine("ITRD", false);
        lineITRD.setLineType("ITRD");
        lineITRD.setFieldNames(Arrays.asList(
                "FIELDNAME","LANG","TRADUCTION"
        ));
        fileStructure.addLineStructure(lineITRD);

        StructuredFile.StructuredLine lineM = new StructuredFile.StructuredLine("M", false);
        lineM.setLineType("M");
        lineM.setFieldNames(Arrays.asList(
                "ILLUST","FACON","PAGIN","NBPHOTO","WEIPAP1","WEICOUV"
        ));
        fileStructure.addLineStructure(lineM);

        StructuredFile.StructuredLine lineMTRD = new StructuredFile.StructuredLine("MTRD", false);
        lineMTRD.setLineType("MTRD");
        lineMTRD.setFieldNames(Arrays.asList(
                "FACON","LANG","TRADUCTION"
        ));
        fileStructure.addLineStructure(lineMTRD);

        fileStructure.setFieldSeparator("|");
        return fileStructure;
    }
}
