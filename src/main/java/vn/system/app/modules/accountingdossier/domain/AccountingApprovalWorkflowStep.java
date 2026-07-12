package vn.system.app.modules.accountingdossier.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.modules.accountingdossier.domain.enums.ApprovalRule;
import vn.system.app.modules.accountingdossier.domain.enums.ApproverStrategy;
import vn.system.app.modules.accountingdossier.domain.enums.PositionReferenceType;
import vn.system.app.modules.accountingdossier.domain.enums.PositionResolverScope;

@Entity
@Table(name = "accounting_approval_workflow_steps", indexes = {
        @Index(name = "idx_acc_wf_step_template_order", columnList = "template_id,step_order")
})
@Getter
@Setter
public class AccountingApprovalWorkflowStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private AccountingApprovalWorkflowTemplate template;

    @Column(name = "step_key", nullable = false, length = 80)
    private String stepKey;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "step_name", nullable = false, length = 255)
    private String stepName;

    @Enumerated(EnumType.STRING)
    @Column(name = "approver_strategy", nullable = false, length = 50)
    private ApproverStrategy approverStrategy;

    @Column(name = "approver_ref_id", length = 255)
    private String approverRefId;

    @Enumerated(EnumType.STRING)
    @Column(name = "position_reference_type", length = 30)
    private PositionReferenceType positionReferenceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "position_resolver_scope", length = 40)
    private PositionResolverScope positionResolverScope;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_rule", nullable = false, length = 30)
    private ApprovalRule approvalRule = ApprovalRule.ANY_ONE;

    @Column(name = "minimum_approvals")
    private Integer minimumApprovals;

    @Column(nullable = false)
    private boolean required = true;

    @Column(name = "sla_minutes")
    private Integer slaMinutes;

    @Column(name = "allow_delegation", nullable = false)
    private boolean allowDelegation = false;

    @Column(name = "allow_forward", nullable = false)
    private boolean allowForward = false;

    @Column(name = "allow_same_approver_collapse", nullable = false)
    private boolean allowSameApproverCollapse = false;
}
