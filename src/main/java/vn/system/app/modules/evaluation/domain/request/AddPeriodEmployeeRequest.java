package vn.system.app.modules.evaluation.domain.request;

import lombok.Data;

@Data
public class AddPeriodEmployeeRequest {
    private String employeeId;
    private String directManagerId;
    private Long templateId;
}
