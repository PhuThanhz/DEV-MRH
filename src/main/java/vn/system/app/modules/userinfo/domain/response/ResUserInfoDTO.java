package vn.system.app.modules.userinfo.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

import vn.system.app.modules.userinfo.domain.UserInfo.Gender;

@Getter
@Setter
public class ResUserInfoDTO {

    private Long id;

    private UserBasic user;

    private String employeeCode;
    private String phone;
    private Instant dateOfBirth;
    private Gender gender;
    private Instant startDate;
    private Instant contractSignDate;
    private Instant contractExpireDate;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @Getter
    @Setter
    public static class UserBasic {
        private Long id;
        private String name;
        private String email;
    }
}