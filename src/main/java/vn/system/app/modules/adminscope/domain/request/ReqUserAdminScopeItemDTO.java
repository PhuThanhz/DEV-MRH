package vn.system.app.modules.adminscope.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUserAdminScopeItemDTO {

    @NotBlank(message = "scopeType không được để trống")
    private String scopeType;

    @NotNull(message = "companyId không được để trống")
    private Long companyId;

    private Long departmentId;
}
