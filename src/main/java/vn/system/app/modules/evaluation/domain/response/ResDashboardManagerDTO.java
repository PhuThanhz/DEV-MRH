package vn.system.app.modules.evaluation.domain.response;

import lombok.Data;
import java.util.List;

@Data
public class ResDashboardManagerDTO {
    private Integer totalEmployees;
    private Integer notStartedCount;
    private Integer pendingReviewCount;
    private Integer reviewedCount;
    private Integer approvedCount;
    private Integer revisionNeededCount;
    private List<ResEvaluationRecordDTO> records;
}
