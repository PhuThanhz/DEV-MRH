package vn.system.app.modules.user.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.userinfo.domain.UserInfo;

@Getter
@Setter
public class ResCreateUserDTO {

    private long id;
    private String name;
    private String email;
    private boolean active;
    private Instant createdAt;

    private UserInfoBasic userInfo;

    @Getter
    @Setter
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