package vn.system.app.modules.accountingdossier.domain.response;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResAccountingDossierApprovalStepDTO {
    private Long id;
    private Long dossierId;
    private int stepOrder;
    private String stepName;
    private String approverType;
    private String approverUserId;
    private String approverName;
    private String status;
    private String actionNote;
    private Instant actedAt;
    private Instant createdAt;
}
