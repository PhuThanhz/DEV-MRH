package vn.system.app.modules.user.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateUserDTO {

    @NotNull(message = "User id không được để trống")
    private Long id;

    private String name;

    // ⭐ XÓA address
    // ⭐ THÊM active
    private Boolean active;

    private Long roleId;
}