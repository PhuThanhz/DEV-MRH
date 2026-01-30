package vn.system.app.modules.companyprocedure.domain.response;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.companyprocedure.domain.enums.ProcedureStatus;

@Getter
@Setter
public class CompanyProcedureResponse {

    private Long id;

    // ===== Company =====
    private String companyCode;
    private String companyName;

    // ===== Department =====
    private Long departmentId;
    private String departmentName;

    // ===== Section / Team =====
    private Long sectionId;
    private String sectionName;

    // ===== Procedure =====
    private String procedureName;
    private String fileUrl;

    // Trạng thái quy trình
    private ProcedureStatus status;

    // Kế hoạch năm
    private Integer planYear;

    // Ghi chú
    private String note;

    // Audit
    private Instant createdAt;
    private Instant updatedAt;
}
