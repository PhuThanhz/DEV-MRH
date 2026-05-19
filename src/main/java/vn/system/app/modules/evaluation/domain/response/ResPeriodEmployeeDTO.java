package vn.system.app.modules.evaluation.domain.response;

import lombok.Data;
import vn.system.app.modules.evaluation.domain.enums.PeriodEmployeeStatus;

@Data
public class ResPeriodEmployeeDTO {
    private Long id;
    private Long periodId;
    private ResEvaluationRecordDTO.ResEmployeeInfo employee;
    private ResEvaluationRecordDTO.ResEmployeeInfo directManager;
    private ResEvaluationRecordDTO.ResEmployeeInfo indirectManager;
    private ResTemplateDTO template;
    private PeriodEmployeeStatus status;
}
