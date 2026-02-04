package vn.system.app.modules.salarygrade.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateSalaryGradeDTO {

    @NotBlank(message = "ContextType không được để trống")
    private String contextType;

    @NotNull(message = "ContextId không được để trống")
    private Long contextId;

    @NotNull(message = "GradeLevel không được để trống")
    private Integer gradeLevel;
}