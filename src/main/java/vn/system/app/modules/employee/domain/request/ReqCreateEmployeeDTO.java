package vn.system.app.modules.employee.domain.request;

import java.time.Instant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.userinfo.domain.UserInfo;

@Getter
@Setter
public class ReqCreateEmployeeDTO {

    // ===== USER =====
    @NotBlank(message = "Tên không được để trống")
    private String name;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    private Boolean active;

    // ===== USER INFO =====
    private String employeeCode;
    private String phone;
    private Instant dateOfBirth;
    private UserInfo.Gender gender;
    private Instant startDate;
    private Instant contractSignDate;
    private Instant contractExpireDate;
}