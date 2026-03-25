package vn.system.app.modules.user.domain.request;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import vn.system.app.modules.userinfo.domain.UserInfo;

@Getter
@Setter
public class ReqUpdateUserDTO {

    // ======= User =======
    @NotNull(message = "User id không được để trống")
    private Long id;

    private String name;
    private Boolean active;
    private Long roleId;

    // ======= UserInfo (optional) =======
    private String employeeCode;
    private String phone;
    private Instant dateOfBirth;
    private UserInfo.Gender gender;
    private Instant startDate;
    private Instant contractSignDate;
    private Instant contractExpireDate;
}