package rmn.ETL.stream.process.P2;

import com.example.common_library.processes.P2_Common_TransformationProcess;
import rmn.ETL.stream.entities.ARTICLEX3;
import rmn.ETL.stream.entities.ARTICLEX3_UR;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class P2_ArticleX3_ArticleUR_TransformationProcess
        extends P2_Common_TransformationProcess<ARTICLEX3, ARTICLEX3_UR> {

    public P2_ArticleX3_ArticleUR_TransformationProcess() {
        super(ARTICLEX3.class, ARTICLEX3_UR.class);
    }

    @Override
    public ARTICLEX3_UR transformToTargetEntity(ARTICLEX3 sourceEntity) {
        ARTICLEX3_UR targetEntity = new ARTICLEX3_UR();

        targetEntity.setCode(getFieldValue(sourceEntity, "I", "ITMREF"));
        targetEntity.setState(getFieldValue(sourceEntity, "I", "ITMSTA"));
        targetEntity.setShortDescription(getFieldValue(sourceEntity, "I", "DES1AXX"));
        targetEntity.setDescription(getFieldValue(sourceEntity, "I", "DES2AXX"));
        targetEntity.setPattern(getFieldValue(sourceEntity, "I", "TSICOD2"));
        targetEntity.setSalesUnit(getFieldValue(sourceEntity, "I", "SAU"));
        targetEntity.setRmnEntityId("");

        targetEntity.setLotTypes(createLotTypes());
        targetEntity.setItems(createItems(sourceEntity));
        targetEntity.setHierarchies(createHierarchies(sourceEntity));
        targetEntity.setAttributes(createAttributes(sourceEntity));
        targetEntity.setVats(createVats(sourceEntity));

        log.info("Transformed to ARTICLEX3_UR: {}", targetEntity);
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

    private List<ARTICLEX3_UR.LotType> createLotTypes() {
        List<ARTICLEX3_UR.LotType> lotTypes = new ArrayList<>();
        ARTICLEX3_UR.LotType lotType = new ARTICLEX3_UR.LotType();
        lotType.setLotTypeCode("REF");
        lotType.setRemove(false);
        lotTypes.add(lotType);
        return lotTypes;
    }

    private List<ARTICLEX3_UR.Item> createItems(ARTICLEX3 sourceEntity) {
        List<ARTICLEX3_UR.Item> items = new ArrayList<>();
        ARTICLEX3_UR.Item item = new ARTICLEX3_UR.Item();
        item.setCode(getFieldValue(sourceEntity, "I", "ITMREF"));

        ARTICLEX3_UR.Item.Barcodes barcodes = new ARTICLEX3_UR.Item.Barcodes();
        ARTICLEX3_UR.Item.Barcodes.PrimaryBarcode primaryBarcode = new ARTICLEX3_UR.Item.Barcodes.PrimaryBarcode();
        primaryBarcode.setBarcodeValue(getFieldValue(sourceEntity, "I", "EANCOD"));
        primaryBarcode.setRemove(false);
        barcodes.setPrimaryBarcode(primaryBarcode);

        ARTICLEX3_UR.Item.Barcodes.SecondaryBarcode secondaryBarcode = new ARTICLEX3_UR.Item.Barcodes.SecondaryBarcode();
        secondaryBarcode.setType("COUR");
        secondaryBarcode.setBarcodeValue(getFieldValue(sourceEntity, "I", "YCODCOURT"));
        secondaryBarcode.setRemove(false);
        barcodes.setSecondaryBarcode(secondaryBarcode);

        item.setBarcodes(barcodes);
        items.add(item);
        return items;
    }

    private List<ARTICLEX3_UR.Hierarchy> createHierarchies(ARTICLEX3 sourceEntity) {
        List<ARTICLEX3_UR.Hierarchy> hierarchies = new ArrayList<>();
        ARTICLEX3_UR.Hierarchy hierarchy = new ARTICLEX3_UR.Hierarchy();
        hierarchy.setHierarchyLevel(getFieldValue(sourceEntity, "I", "BPSNUM"));
        hierarchies.add(hierarchy);
        return hierarchies;
    }

    private List<ARTICLEX3_UR.Attribute> createAttributes(ARTICLEX3 sourceEntity) {
        List<ARTICLEX3_UR.Attribute> attributes = new ArrayList<>();

        attributes.add(createAttribute("CATEG", getFieldValue(sourceEntity, "I", "TCLCOD")));
        attributes.add(createAttribute("CLEGL", getFieldValue(sourceEntity, "I", "TSICOD1")));
        attributes.add(createAttribute("SECTEUR", getFieldValue(sourceEntity, "I", "TSICOD0")));

        return attributes;
    }

    private ARTICLEX3_UR.Attribute createAttribute(String code, String value) {
        ARTICLEX3_UR.Attribute attribute = new ARTICLEX3_UR.Attribute();
        attribute.setAttributeCode(code);
        attribute.setAttributeValue(value);
        attribute.setRemove(false);
        return attribute;
    }

    private List<ARTICLEX3_UR.Vat> createVats(ARTICLEX3 sourceEntity) {
        List<ARTICLEX3_UR.Vat> vats = new ArrayList<>();
        ARTICLEX3_UR.Vat vat = new ARTICLEX3_UR.Vat();
        vat.setVatCode(getFieldValue(sourceEntity, "I", "VACITM0"));
        vat.setCountryCode("FR");
        vat.setRemove(false);
        vats.add(vat);
        return vats;
    }
}
