package vn.system.app.modules.user.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.userinfo.domain.UserInfo;

@Getter
@Setter
public class ResUpdateUserDTO {

    private String id; // 🔥 đổi từ long -> String (UUID)
    private String name;
    private boolean active;
    private Instant updatedAt;

    // 🔥 THÊM ĐOẠN NÀY
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