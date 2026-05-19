package vn.system.app.modules.evaluation.domain.response;

import lombok.Data;
import vn.system.app.modules.evaluation.domain.enums.TemplateType;

@Data
public class ResPeriodTemplateDTO {
    private Long id;
    private Long periodId;
    private TemplateType applyToRole;
    private ResTemplateDTO template;
}
