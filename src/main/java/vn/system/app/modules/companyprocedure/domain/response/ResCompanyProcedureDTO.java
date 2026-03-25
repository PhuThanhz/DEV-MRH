package vn.system.app.modules.companyprocedure.domain.response;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResCompanyProcedureDTO {

    private Long id;

    // ===== Company =====
    private String companyCode;
    private String companyName;

    // ===== Department =====
    private Long departmentId;
    private String departmentName;

    // ===== Section =====
    private Long sectionId;
    private String sectionName;

    // ===== Procedure =====
    private String procedureName;
    private String status;
    private Integer planYear;
    private List<String> fileUrls; // ← đổi từ String fileUrl
    private String note;
    private boolean active;
    private Integer version; // ← thêm vào đây

    // ===== Audit =====
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}