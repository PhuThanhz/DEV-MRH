package vn.system.app.modules.permissionassignment.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResPermissionAssignmentDTO {

    private Long id;

    // ===== CONTENT =====
    private Long permissionContentId;

    // ===== DEPARTMENT =====
    private Long departmentId;
    private String departmentName;

    // ===== JOB TITLE =====
    private Long departmentJobTitleId;
    private Long jobTitleId;
    private String jobTitleName;

    // ===== PROCESS ACTION =====
    private Long processActionId;
    private String processActionCode;
    private String processActionName;
}
