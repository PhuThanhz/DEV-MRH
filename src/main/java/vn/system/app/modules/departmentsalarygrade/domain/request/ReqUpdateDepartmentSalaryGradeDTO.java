package vn.system.app.modules.departmentsalarygrade.domain.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateDepartmentSalaryGradeDTO {

    @NotNull(message = "gradeLevel không được để trống")
    @Min(value = 1, message = "gradeLevel phải ≥ 1")
    private Integer gradeLevel;
}
