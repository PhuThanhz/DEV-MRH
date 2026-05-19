package vn.system.app.modules.evaluation.domain.request;

import lombok.Data;

@Data
public class ApprovalRequest {
    private Boolean approved;
    private String rejectionReason;
}
