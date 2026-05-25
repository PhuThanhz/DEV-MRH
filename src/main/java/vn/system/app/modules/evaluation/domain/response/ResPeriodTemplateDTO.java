package vn.system.app.modules.evaluation.domain.response;

import lombok.Data;

@Data
public class ResPeriodTemplateDTO {
    private Long id;
    private Long periodId;
    private ResTemplateDTO template;
}
