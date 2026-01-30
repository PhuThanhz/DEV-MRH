package vn.system.app.modules.sectionjobtitle.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResSectionJobTitleDTO {

    private Long id;
    private Integer status;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    private JobTitleInfo jobTitle;
    private SectionInfo section;

    @Getter
    @Setter
    public static class JobTitleInfo {
        private Long id;
        private String nameVi;
    }

    @Getter
    @Setter
    public static class SectionInfo {
        private Long id;
        private String name;
    }
}
