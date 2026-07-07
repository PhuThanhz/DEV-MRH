package vn.system.app.modules.departmentobjective.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResDepartmentMissionSummaryDTO {
    private Long departmentId;
    private String departmentName;
    private Long companyId;
    private String companyName;
    private Long objectiveCount;
    private Long taskCount;
    private Long authorityCount;
    private LocalDate issueDate;
    private Instant lastUpdatedAt;
    private String missionStatus;
    private Integer version;
    private Instant issuedAt;

    public ResDepartmentMissionSummaryDTO(
            Long departmentId,
            String departmentName,
            Long companyId,
            String companyName,
            Long objectiveCount,
            Long taskCount,
            Long authorityCount,
            LocalDate issueDate,
            Instant lastUpdatedAt) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.companyId = companyId;
        this.companyName = companyName;
        this.objectiveCount = objectiveCount;
        this.taskCount = taskCount;
        this.authorityCount = authorityCount;
        this.issueDate = issueDate;
        this.lastUpdatedAt = lastUpdatedAt;
    }
}
