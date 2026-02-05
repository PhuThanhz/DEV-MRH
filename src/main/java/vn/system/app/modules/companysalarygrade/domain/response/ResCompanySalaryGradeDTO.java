package vn.system.app.modules.companysalarygrade.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ResCompanySalaryGradeDTO {

    private Long id;
    private Long companyJobTitleId;
    private Integer gradeLevel;
    private boolean active;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
