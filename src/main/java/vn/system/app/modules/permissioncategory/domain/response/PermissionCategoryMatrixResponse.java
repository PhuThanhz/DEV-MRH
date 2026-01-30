package vn.system.app.modules.permissioncategory.domain.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionCategoryMatrixResponse {

    private Category category;
    private List<ContentRow> contents;

    // ================= CATEGORY =================
    @Getter
    @Setter
    public static class Category {
        private Long id;
        private String code;
        private String name;
    }

    // ================= CONTENT =================
    @Getter
    @Setter
    public static class ContentRow {
        private Long contentId;
        private String contentName;
        private List<JobTitlePermission> jobTitles;
    }

    // ================= JOB TITLE + ACTION =================
    @Getter
    @Setter
    public static class JobTitlePermission {
        private Long departmentJobTitleId;
        private Long jobTitleId;
        private String jobTitleName;
        private String processActionCode; // XD, RS, TĐ, PD, TH, KS, TB
    }
}
