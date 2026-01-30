package vn.system.app.modules.permissioncontent.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreatePermissionContentDTO {

    @NotBlank(message = "Tên nội dung không được để trống")
    private String name;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;
}
