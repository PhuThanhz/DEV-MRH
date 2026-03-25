package vn.system.app.modules.companyprocedure.domain.response;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResCompanyProcedureHistoryDTO {

    private Long id;
    private Long procedureId;
    private Integer version;
    private String procedureName;
    private String status;
    private Integer planYear;
    private List<String> fileUrls; // ← đổi từ String fileUrl
    private String note;
    private String departmentName;
    private String sectionName;
    private Instant changedAt;
    private String changedBy;
    private String action; // ← THÊM: "EDIT" hoặc "REVISE"
}