package vn.system.app.modules.accountingdossier.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import vn.system.app.modules.accountingdossier.domain.enums.ApproverType;

@Converter(autoApply = true)
public class ApproverTypeConverter implements AttributeConverter<ApproverType, String> {
    @Override
    public String convertToDatabaseColumn(ApproverType attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public ApproverType convertToEntityAttribute(String dbData) {
        return ApproverType.fromString(dbData);
    }
}
