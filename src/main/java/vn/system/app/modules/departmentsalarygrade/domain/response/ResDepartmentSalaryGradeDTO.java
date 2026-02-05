package vn.system.app.modules.departmentsalarygrade.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResDepartmentSalaryGradeDTO {
    private Long id;
    private Long departmentJobTitleId;
    private Integer gradeLevel;

    private boolean active;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
