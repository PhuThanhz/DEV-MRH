package vn.system.app.modules.departmentsalarygrade.domain.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateDepartmentSalaryGradeDTO {

    @NotNull(message = "departmentJobTitleId không được để trống")
    @Min(value = 1, message = "departmentJobTitleId phải ≥ 1")
    private Long departmentJobTitleId;

    @NotNull(message = "gradeLevel không được để trống")
    @Min(value = 1, message = "gradeLevel phải ≥ 1")
    private Integer gradeLevel;
}
