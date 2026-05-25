package vn.system.app.modules.evaluation.domain.response;

import lombok.Data;
import vn.system.app.modules.evaluation.domain.enums.PeriodStatus;
import java.time.Instant;

@Data
public class ResPeriodDTO {
    private Long id;
    private String name;
    private String description;
    private PeriodStatus status;
    private Instant employeeStartDate;
    private Instant employeeDeadline;
    private Instant managerDeadline;
    private Instant approvalDeadline;
    private Instant createdAt;
    private Instant updatedAt;
    
    private CompanyDTO company;

    @Data
    public static class CompanyDTO {
        private Long id;
        private String name;
    }
}
