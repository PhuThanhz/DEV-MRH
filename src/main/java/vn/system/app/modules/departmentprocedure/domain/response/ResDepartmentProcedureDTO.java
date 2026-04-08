package vn.system.app.modules.departmentprocedure.domain.response;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResDepartmentProcedureDTO {

    private Long id;

    private String companyCode;
    private String companyName;

    private Long departmentId;
    private String departmentName;

    private Long sectionId;
    private String sectionName;
    private String procedureCode; // ← THÊM MỚI
    private String procedureName;
    private String status;
    private Integer planYear;
    private List<String> fileUrls; // ← đổi từ String fileUrl
    private String note;
    private boolean active;
    private Integer version;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}