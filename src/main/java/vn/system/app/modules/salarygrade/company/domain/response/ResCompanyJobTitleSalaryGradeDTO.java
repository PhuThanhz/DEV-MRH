package vn.system.app.modules.salarygrade.company.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResCompanyJobTitleSalaryGradeDTO {

    private Long id;
    private Long companyJobTitleId;
    private Integer gradeLevel;
    private boolean active;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
