package vn.system.app.modules.accountingdossier.domain.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.accountingdossier.domain.enums.ApprovalRule;
import vn.system.app.modules.accountingdossier.domain.enums.ApproverStrategy;

@Getter
@Setter
@Builder
public class ResAccountingApprovalPreviewDTO {
    private Long dossierId;
    private String source;
    private Long templateId;
    private String templateCode;
    private String templateName;
    private Integer templateVersion;
    private boolean valid;
    private List<String> warnings;
    private List<String> blockingErrors;
    private List<StepPreviewDTO> steps;

    @Getter
    @Setter
    @Builder
    public static class StepPreviewDTO {
        private Integer stepOrder;
        private String stepKey;
        private String stepName;
        private ApproverStrategy approverStrategy;
        private ApprovalRule approvalRule;
        private String approverUserId;
        private String assigneeLabel;
        private Integer slaMinutes;
        private boolean required;
    }
}
