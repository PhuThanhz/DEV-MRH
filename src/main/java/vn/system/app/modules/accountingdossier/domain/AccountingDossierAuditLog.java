package vn.system.app.modules.accountingdossier.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;
import java.time.Instant;

@Entity
@Table(name = "accounting_dossier_audit_log")
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

    private Instant createdAt;
    private String createdBy;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("system");
        this.actorUserId = SecurityUtil.getCurrentUserId().orElse(this.createdBy);
    }
}
