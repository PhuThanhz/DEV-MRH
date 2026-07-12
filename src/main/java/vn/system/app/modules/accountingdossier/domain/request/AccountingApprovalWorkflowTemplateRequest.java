package vn.system.app.modules.accountingdossier.domain.request;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.accountingdossier.domain.enums.ApprovalRule;
import vn.system.app.modules.accountingdossier.domain.enums.ApproverStrategy;
import vn.system.app.modules.accountingdossier.domain.enums.PositionReferenceType;
import vn.system.app.modules.accountingdossier.domain.enums.PositionResolverScope;
import vn.system.app.modules.accountingdossier.domain.enums.WorkflowScopeType;

@Getter
@Setter
public class AccountingApprovalWorkflowTemplateRequest {

    @NotBlank(message = "Mã luồng không được để trống")
    private String code;

    @NotBlank(message = "Tên luồng không được để trống")
    private String name;

    private Long companyId;
    private Long dossierCategoryId;
    private String businessType;
    private Integer priority = 100;
    private boolean defaultTemplate = false;
    private Instant effectiveFrom;
    private Instant effectiveTo;

    @NotEmpty(message = "Luồng duyệt cần ít nhất một bước")
    private List<StepRequest> steps = new ArrayList<>();

    private List<ScopeRequest> scopes = new ArrayList<>();

    @Getter
    @Setter
    public static class StepRequest {
        private String stepKey;
        private Integer stepOrder;
        private String stepName;
        private ApproverStrategy approverStrategy;
        private String approverRefId;
        private PositionReferenceType positionReferenceType;
        private PositionResolverScope positionResolverScope;
        private ApprovalRule approvalRule = ApprovalRule.ANY_ONE;
        private Integer minimumApprovals;
        private boolean required = true;
        private Integer slaMinutes;
        private boolean allowDelegation;
        private boolean allowForward;
        private boolean allowSameApproverCollapse;
    }

    @Getter
    @Setter
    public static class ScopeRequest {
        private WorkflowScopeType scopeType;
        private Long scopeId;
        private boolean includeChildren;
    }
}
