package vn.system.app.modules.accountingdossier.domain;

import java.time.Instant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "accounting_dossier_approval_steps")
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

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Column(name = "step_name", nullable = false, length = 150)
    private String stepName;

    @Column(name = "approver_type", nullable = false, length = 50)
    private String approverType; // DEPARTMENT_MANAGER, ACCOUNTANT, CHIEF_ACCOUNTANT

    @Column(name = "approver_user_id", length = 36)
    private String approverUserId; // UUID String of User

    @Column(name = "status", nullable = false, length = 40)
    private String status; // PENDING, CURRENT, APPROVED, RETURNED, REJECTED, SKIPPED

    @Column(name = "action_note", length = 1000)
    private String actionNote;

    @Column(name = "acted_at")
    private Instant actedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean active = true;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
        this.active = true;
    }
}
