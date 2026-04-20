package vn.system.app.modules.userposition.domain.response;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResUserPositionDTO {

    private Long id;
    private String source;
    private boolean active;

    // ← THÊM
    private UserInfo user;

    private JobTitleInfo jobTitle;
    private CompanyInfo company;
    private DepartmentInfo department;
    private SectionInfo section;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    // ← THÊM nested class
    @Getter
    @Setter
    public static class UserInfo {
        private String id;
        private String name;
        private String email;
    }

    @Getter
    @Setter
    public static class JobTitleInfo {
        private Long id;
        private String nameVi;
        private String nameEn;
        private String positionCode;
        private String band;
        private Integer levelNumber;
        private Integer bandOrder;
    }

    @Getter
    @Setter
    public static class CompanyInfo {
        private Long id;
        private String name;
    }

    @Getter
    @Setter
    public static class DepartmentInfo {
        private Long id;
        private String name;
    }

    @Getter
    @Setter
    public static class SectionInfo {
        private Long id;
        private String name;
    }
}