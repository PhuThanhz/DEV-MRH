package vn.system.app.modules.sectionjobtitle.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResSectionJobTitleDTO {

    private Long id;
    private boolean active;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    private JobTitleInfo jobTitle;
    private SectionInfo section;
    private DepartmentInfo department; // ⭐ THÊM PHÒNG BAN

    /*
     * ======================================================
     * JOB TITLE INFO (bao gồm cấp bậc)
     * ======================================================
     */
    @Getter
    @Setter
    public static class JobTitleInfo {
        private Long id;
        private String nameVi;

        // ⭐ Cấp bậc đầy đủ
        private String positionCode;
        private String band;
        private Integer level;

        private Integer bandOrder;
        private Integer levelNumber;
    }

    /*
     * ======================================================
     * SECTION INFO
     * ======================================================
     */
    @Getter
    @Setter
    public static class SectionInfo {
        private Long id;
        private String name;
    }

    /*
     * ======================================================
     * DEPARTMENT INFO (⭐ mới thêm)
     * ======================================================
     */
    @Getter
    @Setter
    public static class DepartmentInfo {
        private Long id;
        private String name;
    }
}
