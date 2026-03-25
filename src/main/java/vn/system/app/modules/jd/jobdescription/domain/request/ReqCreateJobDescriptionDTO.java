package vn.system.app.modules.jd.jobdescription.domain.request;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import vn.system.app.modules.jd.jobdescriptionrequirement.domain.request.ReqRequirementDTO;
import vn.system.app.modules.jd.jobdescriptiontask.domain.request.ReqTaskDTO;
import vn.system.app.modules.jd.jobdescriptionposition.domain.request.ReqPositionDTO;

@Getter
@Setter
public class ReqCreateJobDescriptionDTO {

    /*
     * =========================
     * RELATION
     * =========================
     */

    private Long companyId;

    private Long departmentId;

    private Long companyJobTitleId;

    private Long departmentJobTitleId;

    private Long sectionJobTitleId;

    /*
     * =========================
     * JD INFORMATION
     * =========================
     */

    private String code;

    private String reportTo;

    private String belongsTo;

    private String collaborateWith;

    /*
     * =========================
     * STATUS
     * =========================
     */

    private Instant effectiveDate;

    /*
     * =========================
     * REQUIREMENT
     * =========================
     */

    private ReqRequirementDTO requirements;

    /*
     * =========================
     * TASKS
     * =========================
     */

    private List<ReqTaskDTO> tasks;

    /*
     * =========================
     * POSITIONS
     * =========================
     */

    private List<ReqPositionDTO> positions;

}
