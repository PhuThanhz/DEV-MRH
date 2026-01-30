package vn.system.app.modules.section.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResSectionDTO {

    private Long id;
    private String code;
    private String name;

    private DepartmentInfo department;

    private Integer status;
    private Instant createdAt;
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;

    @Getter
    @Setter
    public static class DepartmentInfo {
        private Long id;
        private String name;
    }
}
