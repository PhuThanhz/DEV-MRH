package vn.system.app.modules.salarygrade.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateSalaryGradeDTO {

    @NotNull(message = "OrgJobTitleId không được để trống")
    private Long orgJobTitleId;

    @NotNull(message = "GradeLevel không được để trống")
    private Integer gradeLevel;
}
