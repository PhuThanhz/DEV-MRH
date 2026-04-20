package vn.system.app.modules.employee.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResCreateEmployeeDTO {

    private String id;
    private String name;
    private String email;
    private Boolean active;

    private Instant createdAt;

    private UserInfoBasic userInfo;

    @Getter
    @Setter
    public static class UserInfoBasic {
        private String employeeCode;
        private String phone;
    }
}