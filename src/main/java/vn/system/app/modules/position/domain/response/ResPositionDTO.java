package vn.system.app.modules.position.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResPositionDTO {

    private Long id;
    private boolean active;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    private CompanyInfo company;
    private DepartmentInfo department;
    private SectionInfo section;
    private JobTitleInfo jobTitle;

    private ReportsToInfo reportsTo;

    // ⭐ USER ĐANG NGỒI GHẾ
    private EmployeeInfo employee;

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

    @Getter
    @Setter
    public static class JobTitleInfo {
        private Long id;
        private String nameVi;
        private String positionCode;
        private Integer bandOrder;
        private Integer levelNumber;
    }

    @Getter
    @Setter
    public static class ReportsToInfo {
        private Long id;
        private String jobTitleName;
    }

    // ⭐ THÔNG TIN USER
    @Getter
    @Setter
    public static class EmployeeInfo {
        private Long id;
        private String name;
        private String email;
    }
}