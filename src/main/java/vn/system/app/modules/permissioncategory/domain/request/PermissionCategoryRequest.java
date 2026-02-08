package vn.system.app.modules.permissioncategory.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionCategoryRequest {

    @NotBlank(message = "code không được để trống")
    private String code;

    @NotBlank(message = "name không được để trống")
    private String name;

    @NotNull(message = "departmentId không được để trống")
    private Long departmentId;

    /**
     * Trạng thái sử dụng
     * - true : bật
     * - false : tắt
     *
     * Không bắt buộc khi create (mặc định = true)
     */
    private Boolean active;
}
