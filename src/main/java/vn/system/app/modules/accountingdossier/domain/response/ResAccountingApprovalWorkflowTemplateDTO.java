package vn.system.app.modules.accountingdossier.domain.response;

import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.accountingdossier.domain.enums.ApprovalRule;
import vn.system.app.modules.accountingdossier.domain.enums.ApproverStrategy;
import vn.system.app.modules.accountingdossier.domain.enums.PositionReferenceType;
import vn.system.app.modules.accountingdossier.domain.enums.PositionResolverScope;
import vn.system.app.modules.accountingdossier.domain.enums.WorkflowScopeType;
import vn.system.app.modules.accountingdossier.domain.enums.WorkflowTemplateStatus;

@Getter
@Setter
@Builder
public class ResAccountingApprovalWorkflowTemplateDTO {
    private Long id;
    private String code;
    private String name;
    private Long companyId;
    private Long dossierCategoryId;
    private String businessType;
    private Integer priority;
    private boolean defaultTemplate;
    private WorkflowTemplateStatus status;
    private Integer version;
    private Instant effectiveFrom;
    private Instant effectiveTo;
    private List<StepDTO> steps;
    private List<ScopeDTO> scopes;

    @Getter
    @Setter
    @Builder
    public static class StepDTO {
        private Long id;
        private String stepKey;
        private Integer stepOrder;
        private String stepName;
        private ApproverStrategy approverStrategy;
        private String approverRefId;
        private PositionReferenceType positionReferenceType;
        private PositionResolverScope positionResolverScope;
        private ApprovalRule approvalRule;
        private Integer minimumApprovals;
        private boolean required;
        private Integer slaMinutes;
        private boolean allowDelegation;
        private boolean allowForward;
        private boolean allowSameApproverCollapse;
    }

    @Getter
    @Setter
    @Builder
    public static class ScopeDTO {
        private Long id;
        private WorkflowScopeType scopeType;
        private Long scopeId;
        private boolean includeChildren;
    }
}
