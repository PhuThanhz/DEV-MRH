package vn.system.app.modules.accountingdossier.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import vn.system.app.modules.accountingdossier.domain.enums.ApprovalStepStatus;

@Converter(autoApply = true)
public class ApprovalStepStatusConverter implements AttributeConverter<ApprovalStepStatus, String> {
    @Override
    public String convertToDatabaseColumn(ApprovalStepStatus attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public ApprovalStepStatus convertToEntityAttribute(String dbData) {
        return ApprovalStepStatus.fromString(dbData);
    }
}
