package vn.system.app.modules.salarygrade.company.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateCompanyJobTitleSalaryGradeDTO {

    @NotNull
    private Integer gradeLevel;
}
