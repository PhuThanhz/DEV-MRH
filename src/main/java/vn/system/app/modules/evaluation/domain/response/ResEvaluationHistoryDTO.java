package vn.system.app.modules.evaluation.domain.response;

import java.time.Instant;

import lombok.Data;
import vn.system.app.modules.evaluation.domain.enums.RecordStatus;

@Data
public class ResEvaluationHistoryDTO {
    private Long id;
    private Long recordId;
    private RecordStatus fromStatus;
    private RecordStatus toStatus;
    private ResEvaluationRecordDTO.ResEmployeeInfo performedBy;
    private String note;
    private Instant performedAt;
}
