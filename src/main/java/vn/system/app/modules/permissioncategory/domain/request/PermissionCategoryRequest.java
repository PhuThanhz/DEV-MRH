package vn.system.app.modules.permissioncategory.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionCategoryRequest {

    @NotBlank(message = "code không được để trống")
    private String code;

    @NotBlank(message = "name không được để trống")
    private String name;
}
