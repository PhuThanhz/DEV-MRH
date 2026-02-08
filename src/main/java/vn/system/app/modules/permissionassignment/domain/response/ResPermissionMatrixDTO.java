package vn.system.app.modules.permissionassignment.domain.response;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ResPermissionMatrixDTO {

    private Long contentId;
    private String contentName;
    private Category category;
    private List<DepartmentMatrix> departments;

    @Getter
    @Setter
    public static class Category {
        private Long id;
        private String code;
        private String name;
    }

    @Getter
    @Setter
    public static class DepartmentMatrix {
        private Long departmentId;
        private String departmentName;
        private List<JobTitleMatrix> jobTitles;
    }

    @Getter
    @Setter
    public static class JobTitleMatrix {
        private Long departmentJobTitleId;
        private Long jobTitleId;
        private String jobTitleName;

        private Long processActionId; // 👈 thêm
        private String actionCode; // giữ lại để hiển thị
    }

}
