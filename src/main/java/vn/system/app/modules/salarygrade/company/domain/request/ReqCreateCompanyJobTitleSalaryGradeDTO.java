package vn.system.app.modules.salarygrade.company.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateCompanyJobTitleSalaryGradeDTO {

    @NotNull
    private Long companyJobTitleId;

    @NotNull
    private Integer gradeLevel;
}
