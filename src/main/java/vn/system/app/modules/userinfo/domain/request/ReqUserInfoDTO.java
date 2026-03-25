package vn.system.app.modules.userinfo.domain.request;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import vn.system.app.modules.userinfo.domain.UserInfo.Gender;

@Getter
@Setter
public class ReqUserInfoDTO {

    @NotBlank(message = "Mã nhân viên không được để trống")
    private String employeeCode;

    private String phone;

    private Instant dateOfBirth;

    private Gender gender;

    private Instant startDate;

    private Instant contractSignDate;

    private Instant contractExpireDate;
}