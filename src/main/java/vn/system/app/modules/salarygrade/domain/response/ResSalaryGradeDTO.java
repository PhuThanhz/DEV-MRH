package vn.system.app.modules.salarygrade.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResSalaryGradeDTO {

    private Long id;
    private Integer gradeLevel;
    private Integer status;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    // ===== NGỮ CẢNH CHỨC DANH =====
    private OrgJobTitleInfo orgJobTitle;

    @Getter
    @Setter
    public static class OrgJobTitleInfo {
        private Long id;
        private String orgType; // COMPANY / DEPARTMENT / SECTION
        private Long orgId;

        private JobTitleInfo jobTitle;
    }

    @Getter
    @Setter
    public static class JobTitleInfo {
        private Long id;
        private String nameVi;
    }
}
