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
    private Long recordId;

    // Các trường phản ánh trạng thái thực tế của bản đánh giá
    private String recordStatus;
    private java.time.Instant employeeDeadlineOverride;
    private java.time.Instant managerDeadlineOverride;
    private java.time.Instant approvalDeadlineOverride;
}
