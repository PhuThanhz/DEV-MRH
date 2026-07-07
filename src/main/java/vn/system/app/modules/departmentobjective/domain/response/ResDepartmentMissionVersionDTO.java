package vn.system.app.modules.departmentobjective.domain.response;

import java.time.Instant;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResDepartmentMissionVersionDTO {

    private Long id;

    private Integer version;

    private String title;

    private String changeSummary;

    private LocalDate effectiveDate;

    private LocalDate issueDate;

    private Long objectiveCount;

    private Long taskCount;

    private Long authorityCount;

    private String snapshotJson;

    private String createdBy;

    private String createdByName;

    private Instant createdAt;
}
