package vn.system.app.modules.employee.domain.response;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResEmployeeDTO {

    private Long id;
    private String name;
    private String email;
    private String avatar;
    private Boolean active;

    private Instant createdAt;
    private Instant updatedAt;

    // ===== ROLE =====
    private RoleBasic role;

    // ===== USER INFO =====
    private UserInfoBasic userInfo;

    // ===== POSITION =====
    private List<PositionBasic> positions;

    @Getter
    @Setter
    public static class RoleBasic {
        private Long id;
        private String name;
    }

    @Getter
    @Setter
    public static class UserInfoBasic {
        private String employeeCode;
        private String phone;
        private Instant dateOfBirth;
        private String gender;
        private Instant startDate;
        private Instant contractSignDate;
        private Instant contractExpireDate;
    }

    @Getter
    @Setter
    public static class PositionBasic {
        private Long id;
        private String source;

        private String companyName;
        private String departmentName;
        private String sectionName;

        private String jobTitleNameVi;
    }
}