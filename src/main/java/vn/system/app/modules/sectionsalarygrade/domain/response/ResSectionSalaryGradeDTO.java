package vn.system.app.modules.sectionsalarygrade.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResSectionSalaryGradeDTO {

    private Long id;
    private Long sectionJobTitleId;
    private Integer gradeLevel;

    private boolean active;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
