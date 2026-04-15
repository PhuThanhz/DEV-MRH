package vn.system.app.modules.departmentjobtitle.domain.response;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResDepartmentJobTitleDTO {

    private Long id;
    private boolean active;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    // source: COMPANY | SECTION | DEPARTMENT
    private String source;

    // 🔥 THÊM MỚI: danh sách phòng ban đang dùng jobTitle này
    private List<String> usedInDepartments;

    private JobTitleInfo jobTitle;
    private DepartmentInfo department;

    @Getter
    @Setter
    public static class JobTitleInfo {
        private Long id;
        private String nameVi;
        private String nameEn;

        private String positionCode;
        private String band;
        private Integer level;

        private Integer bandOrder;
        private Integer levelNumber;
    }

    @Getter
    @Setter
    public static class DepartmentInfo {
        private Long id;
        private String name;
    }

}
