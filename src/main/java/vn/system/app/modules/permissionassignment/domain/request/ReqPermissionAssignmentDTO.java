package vn.system.app.modules.permissionassignment.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqPermissionAssignmentDTO {

    @NotNull(message = "permissionContentId không được để trống")
    private Long permissionContentId;

    @NotNull(message = "departmentJobTitleId không được để trống")
    private Long departmentJobTitleId;

    @NotNull(message = "processActionId không được để trống")
    private Long processActionId;
}
