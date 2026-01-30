package vn.system.app.modules.permissioncategoryscope.domain.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionCategoryScopeRequest {

    @NotNull(message = "Danh mục phân quyền không được rỗng")
    private Long permissionCategoryId;

    @NotNull(message = "Phòng ban không được rỗng")
    private Long departmentId;

    @NotEmpty(message = "Danh sách chức danh phòng ban không được rỗng")
    private List<Long> departmentJobTitleIds;
}
