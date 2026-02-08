package vn.system.app.modules.permissionassignment.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqAssignPermissionDTO {

    @NotNull
    private Long departmentJobTitleId;

    @NotNull
    private Long processActionId;
}
