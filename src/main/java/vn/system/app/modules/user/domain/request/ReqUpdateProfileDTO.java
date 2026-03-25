package vn.system.app.modules.user.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateProfileDTO {

    @NotBlank(message = "Tên không được để trống")
    private String name;

    // ⭐ XÓA address
    private String avatar;
}