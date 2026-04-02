package vn.system.app.modules.employee.domain.request;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.userinfo.domain.UserInfo;

@Getter
@Setter
public class ReqUpdateEmployeeDTO {

    @NotNull(message = "Id không được để trống")
    private Long id;

    // ===== USER =====
    private String name;
    private Boolean active;

    // ===== USER INFO =====

    private String phone;
    private Instant dateOfBirth;
    private UserInfo.Gender gender;
    private Instant startDate;
    private Instant contractSignDate;
    private Instant contractExpireDate;
}