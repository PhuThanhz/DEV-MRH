package vn.system.app.modules.evaluation.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BatchApproveResponse {
    private List<Long> successIds;
    private List<FailedApproval> failedRecords;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FailedApproval {
        private Long recordId;
        private String reason;
    }
}
