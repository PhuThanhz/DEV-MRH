package vn.system.app.modules.section.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateSectionDTO {

    @NotBlank(message = "Mã bộ phận không được để trống")
    private String code;

    @NotBlank(message = "Tên bộ phận không được để trống")
    private String name;

    @NotNull(message = "DepartmentId không được để trống")
    private Long departmentId;
}
