package vn.system.app.modules.accountingdossier.domain;

import java.time.Instant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import vn.system.app.modules.accountingdossier.domain.enums.ApproverType;
import vn.system.app.modules.accountingdossier.domain.enums.ApprovalStepStatus;
import vn.system.app.modules.accountingdossier.domain.converter.ApproverTypeConverter;
import vn.system.app.modules.accountingdossier.domain.converter.ApprovalStepStatusConverter;

@Entity
@Table(name = "accounting_dossier_approval_steps", indexes = {
        @Index(name = "idx_acc_step_dossier_active_order", columnList = "dossier_id,active,step_order"),
        @Index(name = "idx_acc_step_dossier_status", columnList = "dossier_id,status,active"),
        @Index(name = "idx_acc_step_approver_status", columnList = "approver_user_id,status,active")
})
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class AccountingDossierApprovalStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dossier_id", nullable = false)
    @JsonIgnoreProperties({ "approvalSteps", "documentItems" })
    private AccountingDossier dossier;

    @Column(name = "instance_id")
    private Long instanceId;

    @Column(name = "step_key", length = 80)
    private String stepKey;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Column(name = "step_name", nullable = false, length = 150)
    private String stepName;

    @Column(name = "approver_type", nullable = false, length = 50)
    @Convert(converter = ApproverTypeConverter.class)
    private ApproverType approverType; // DEPARTMENT_MANAGER, ACCOUNTANT, CHIEF_ACCOUNTANT

    @Column(name = "approver_user_id", length = 36)
    private String approverUserId; // UUID String of User

    @Column(name = "eligible_approver_ids", length = 2000)
    private String eligibleApproverIds;

    @Column(name = "status", nullable = false, length = 40)
    @Convert(converter = ApprovalStepStatusConverter.class)
    private ApprovalStepStatus status; // PENDING, CURRENT, APPROVED, RETURNED, REJECTED, SKIPPED

    @Column(name = "action_note", length = 1000)
    private String actionNote;

    @Column(name = "acted_at")
    private Instant actedAt;

    @Column(name = "due_at")
    private Instant dueAt;

    @Column(name = "sla_minutes")
    private Integer slaMinutes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "allow_delegation", nullable = false)
    private boolean allowDelegation = false;

    @Version
    private Long version = 0L;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
        this.active = true;
        normalizeVersion();
    }

    @PostLoad
    @PreUpdate
    public void normalizeVersion() {
        if (this.version == null) {
            this.version = 0L;
        }
    }
}
