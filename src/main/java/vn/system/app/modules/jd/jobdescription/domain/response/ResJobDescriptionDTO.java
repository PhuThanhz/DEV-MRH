package vn.system.app.modules.jd.jobdescription.domain.response;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import vn.system.app.modules.jd.jobdescriptionrequirement.domain.response.ResRequirementDTO;
import vn.system.app.modules.jd.jobdescriptiontask.domain.response.ResTaskDTO;
import vn.system.app.modules.jd.jobdescriptionposition.domain.response.ResPositionDTO;

@Getter
@Setter
public class ResJobDescriptionDTO {

    /*
     * =========================
     * BASIC
     * =========================
     */

    private Long id;

    private String code;

    private String reportTo;

    private String belongsTo;

    private String collaborateWith;

    /*
     * =========================
     * STATUS
     * =========================
     */

    private String status;

    private Integer version;

    private Instant effectiveDate;

    /*
     * =========================
     * AUDIT
     * =========================
     */

    private Instant createdAt;

    private Instant updatedAt;

    private String createdBy;

    private String updatedBy;

    /*
     * =========================
     * RELATION
     * =========================
     */

    private Long companyId;
    private String companyName;

    private Long departmentId;
    private String departmentName;

    private Long companyJobTitleId;

    private Long departmentJobTitleId;
    private String jobTitleName;

    private Long sectionJobTitleId;

    /*
     * =========================
     * CHILD DATA
     * =========================
     */

    private ResRequirementDTO requirements;

    private List<ResTaskDTO> tasks;

    private List<ResPositionDTO> positions;

    /*
     * =========================
     * REJECT INFO
     * =========================
     */
    private String rejectComment;
    private String rejectorName;
    private String rejectorPosition;
    private String rejectorDepartment;
    private String rejectorPositionCode; // ← THÊM
}