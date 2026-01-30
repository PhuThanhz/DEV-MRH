package vn.system.app.modules.permissioncategoryscope.domain.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResPermissionCategoryScopeGroupedDTO {

    private Category category;
    private Department department;
    private List<JobTitle> jobTitles;

    // ===== CATEGORY =====
    @Getter
    @Setter
    public static class Category {
        private Long id;
        private String code;
        private String name;
    }

    // ===== DEPARTMENT =====
    @Getter
    @Setter
    public static class Department {
        private Long id;
        private String name;
    }

    // ===== JOB TITLE (THEO PHÒNG BAN) =====
    @Getter
    @Setter
    public static class JobTitle {
        private Long departmentJobTitleId;
        private Long jobTitleId;
        private String jobTitleName;
    }
}
