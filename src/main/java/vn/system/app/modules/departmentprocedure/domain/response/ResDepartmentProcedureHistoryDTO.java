package vn.system.app.modules.departmentprocedure.domain.response;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResDepartmentProcedureHistoryDTO {

    private Long id;
    private Long procedureId;
    private Integer version;
    private String procedureCode;
    private String procedureName;
    private String status;
    private Integer planYear;
    private Instant issuedDate; // ← THÊM
    private List<String> fileUrls;
    private String note;
    private String departmentName;
    private String sectionName;
    private Instant changedAt;
    private String changedBy;
    private String action;
}