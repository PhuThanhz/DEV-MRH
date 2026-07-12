package vn.system.app.modules.accountingdossier.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;
import java.time.Instant;

@Entity
@Table(name = "accounting_dossier_audit_log", indexes = {
    @Index(name = "idx_audit_dossier", columnList = "dossier_id,created_at"),
    @Index(name = "idx_audit_bulk_action", columnList = "bulk_action_id")
})
@Getter
@Setter
public class AccountingDossierAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false)
    private AccountingDossier dossier;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "actor_user_id", length = 36)
    private String actorUserId;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "target_type", length = 80)
    private String targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "from_status", length = 80)
    private String fromStatus;

    @Column(name = "to_status", length = 80)
    private String toStatus;

    @Column(name = "before_value", columnDefinition = "TEXT")
    private String beforeValue;

    @Column(name = "after_value", columnDefinition = "TEXT")
    private String afterValue;

    @Column(name = "bulk_action_id", length = 80)
    private String bulkActionId;

    private Instant createdAt;
    private String createdBy;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("system");
        this.actorUserId = SecurityUtil.getCurrentUserId().orElse(this.createdBy);
    }
}
