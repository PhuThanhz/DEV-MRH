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
import vn.system.app.modules.accountingdossier.domain.enums.WorkflowScopeType;

@Entity
@Table(name = "accounting_approval_workflow_scopes", indexes = {
        @Index(name = "idx_acc_wf_scope_lookup", columnList = "scope_type,scope_id,template_id")
})
@Getter
@Setter
public class AccountingApprovalWorkflowScope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private AccountingApprovalWorkflowTemplate template;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 30)
    private WorkflowScopeType scopeType;

    @Column(name = "scope_id")
    private Long scopeId;

    @Column(name = "include_children", nullable = false)
    private boolean includeChildren = false;
}
