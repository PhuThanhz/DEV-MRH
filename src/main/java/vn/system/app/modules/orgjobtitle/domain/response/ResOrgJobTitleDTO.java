package vn.system.app.modules.orgjobtitle.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResOrgJobTitleDTO {

    private Long id;

    // ===== ORG (CHỈ 1 TRONG 3) =====
    private CompanyInfo company;
    private DepartmentInfo department;
    private SectionInfo section;

    private Integer status;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    private JobTitleInfo jobTitle;

    /* ========= JOB TITLE ========= */
    @Getter
    @Setter
    public static class JobTitleInfo {
        private Long id;
        private String nameVi;
    }

    /* ========= COMPANY ========= */
    @Getter
    @Setter
    public static class CompanyInfo {
        private Long id;
        private String name;
    }

    /* ========= DEPARTMENT ========= */
    @Getter
    @Setter
    public static class DepartmentInfo {
        private Long id;
        private String name;
    }

    /* ========= SECTION ========= */
    @Getter
    @Setter
    public static class SectionInfo {
        private Long id;
        private String name;
    }
}
