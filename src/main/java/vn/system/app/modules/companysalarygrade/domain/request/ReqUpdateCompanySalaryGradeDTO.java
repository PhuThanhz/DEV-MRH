package vn.system.app.modules.companysalarygrade.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateCompanySalaryGradeDTO {

    @NotNull(message = "gradeLevel không được null")
    private Integer gradeLevel;
}
