package vn.system.app.modules.permissioncontent.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdatePermissionContentDTO {

    @NotBlank(message = "Tên nội dung không được để trống")
    private String name;
}
