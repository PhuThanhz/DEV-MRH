package vn.system.app.modules.departmentjobtitle.domain.response;

import java.time.Instant;
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

    // >>> THÊM FIELD NÀY <<<
    private String source;

    private JobTitleInfo jobTitle;
    private DepartmentInfo department;

    @Getter
    @Setter
    public static class JobTitleInfo {
        private Long id;
        private String nameVi;
        private String nameEn; // ⭐ THÊM DÒNG NÀY

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
