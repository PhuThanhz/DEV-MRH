package vn.system.app.modules.permissioncategory.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionCategoryResponse {

    private long id;
    private String code;
    private String name;

    private Boolean active;

    private Long departmentId;
    private String departmentName;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
