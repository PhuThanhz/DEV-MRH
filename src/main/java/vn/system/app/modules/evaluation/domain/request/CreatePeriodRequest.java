package vn.system.app.modules.evaluation.domain.request;

import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
public class CreatePeriodRequest {
    private String name;
    private String description;
    private Instant employeeStartDate;
    private Instant employeeDeadline;
    private Instant managerDeadline;
    private Instant approvalDeadline;
    private List<Long> templateIds;
}
