package vn.system.app.modules.departmentjobtitle.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqDepartmentJobTitleDTO {

    @NotNull(message = "JobTitleId không được để trống")
    private Long jobTitleId;

    @NotNull(message = "DepartmentId không được để trống")
    private Long departmentId;
}
