package vn.system.app.modules.evaluation.domain.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ResDashboardApproverDTO {
    private Integer totalEmployees;
    private Integer completedCount;
    private Integer pendingApprovalCount;
    private Integer revisionNeededCount;
    private Map<String, Integer> gradeDistribution;
    private List<ResEvaluationRecordDTO> records;
}
