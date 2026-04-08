package vn.system.app.modules.confidentialprocedure.domain.response;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResConfidentialProcedureHistoryDTO {

    private Long id;
    private Long procedureId;
    private Integer version;
    private String procedureCode;
    private String procedureName;
    private String status;
    private Integer planYear;
    private List<String> fileUrls; // ← đổi từ String fileUrl
    private String note;
    private String departmentName;
    private String sectionName;
    private String action;
    private Instant changedAt;
    private String changedBy;
}