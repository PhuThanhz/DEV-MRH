package vn.system.app.modules.departmentjobtitle.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResDepartmentJobTitleDTO {

    private Long id;
    private Integer status;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    private JobTitleInfo jobTitle;
    private DepartmentInfo department;

    @Getter
    @Setter
    public static class JobTitleInfo {
        private Long id;
        private String nameVi;
    }

    @Getter
    @Setter
    public static class DepartmentInfo {
        private Long id;
        private String name;
    }
}
