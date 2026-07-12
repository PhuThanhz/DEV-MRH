package vn.system.app.modules.accountingdossier.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.accountingdossier.domain.enums.DelegationStatus;

@Entity
@Table(name = "accounting_approval_delegations", indexes = {
        @Index(name = "idx_acc_delegation_lookup", columnList = "delegator_user_id,delegate_user_id,status,valid_from,valid_to"),
        @Index(name = "idx_acc_delegation_company", columnList = "company_id,status")
})
@Getter
@Setter
public class AccountingApprovalDelegation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "delegator_user_id", nullable = false, length = 36)
    private String delegatorUserId;

    @Column(name = "delegate_user_id", nullable = false, length = 36)
    private String delegateUserId;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_to", nullable = false)
    private Instant validTo;

    @Column(name = "scope_type", length = 50)
    private String scopeType;

    @Column(name = "scope_ref_id")
    private Long scopeRefId;

    @Column(length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DelegationStatus status = DelegationStatus.DRAFT;

    private Instant createdAt;
    private String createdBy;
    private Instant revokedAt;
    private String revokedBy;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
        if (this.status == null) {
            this.status = DelegationStatus.DRAFT;
        }
    }
}
