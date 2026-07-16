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
    private PersonPreviewDTO sender;
    private List<StepPreviewDTO> steps;

    @Getter
    @Setter
    @Builder
    public static class PersonPreviewDTO {
        private String userId;
        private String name;
        private String email;
        private String roleName;
        private String jobTitleName;
        private String positionLevelCode;
        private String companyName;
        private String departmentName;
        private String sectionName;
    }

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
        private PersonPreviewDTO assignee;
        private Integer slaMinutes;
        private boolean required;
    }
}
