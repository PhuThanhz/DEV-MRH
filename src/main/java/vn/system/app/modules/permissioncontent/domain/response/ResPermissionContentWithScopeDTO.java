package vn.system.app.modules.permissioncontent.domain.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResPermissionContentWithScopeDTO {

    private PermissionContent permissionContent;
    private List<DepartmentScope> appliedScopes;

    // ===== CONTENT =====
    @Getter
    @Setter
    public static class PermissionContent {
        private Long id;
        private String name;
        private Category category;
    }

    @Getter
    @Setter
    public static class Category {
        private Long id;
        private String code;
        private String name;
    }

    // ===== SCOPE =====
    @Getter
    @Setter
    public static class DepartmentScope {
        private Long departmentId;
        private String departmentName;
        private List<JobTitleScope> jobTitles;
    }

    @Getter
    @Setter
    public static class JobTitleScope {
        private Long departmentJobTitleId;
        private Long jobTitleId;
        private String jobTitleName;
    }
}
