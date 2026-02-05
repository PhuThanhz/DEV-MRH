package vn.system.app.modules.companysalarygrade.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateCompanySalaryGradeDTO {

    @NotNull(message = "companyJobTitleId không được null")
    private Long companyJobTitleId;

    @NotNull(message = "gradeLevel không được null")
    private Integer gradeLevel;
}
