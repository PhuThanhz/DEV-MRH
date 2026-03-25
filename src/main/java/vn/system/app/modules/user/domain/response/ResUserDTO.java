package vn.system.app.modules.user.domain.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.system.app.modules.userinfo.domain.UserInfo;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResUserDTO {
    private long id;
    private String email;
    private String name;
    private String avatar;
    private boolean active;
    private Instant updatedAt;
    private Instant createdAt;

    private RoleUser role;
    private UserInfoBasic userInfo;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoleUser {
        private long id;
        private String name;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfoBasic {
        private String employeeCode;
        private String phone;
        private Instant dateOfBirth;
        private UserInfo.Gender gender;
        private Instant startDate;
        private Instant contractSignDate;
        private Instant contractExpireDate;
    }
}