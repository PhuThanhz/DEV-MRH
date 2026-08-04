package vn.system.app.modules.approvaldelegation.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.approvaldelegation.domain.enums.ApprovalDelegationStatus;
import vn.system.app.modules.user.domain.User;

@Entity
@Table(name = "approval_delegations", indexes = {
        @Index(name = "idx_delegation_module_users", columnList = "module, delegator_user_id, delegate_user_id"),
        @Index(name = "idx_delegation_status_dates", columnList = "status, valid_from, valid_to")
})
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ApprovalDelegation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String module;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delegator_user_id", nullable = false)
    @JsonIgnoreProperties({ "role", "userInfo", "directManager", "subordinates" })
    private User delegatorUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delegate_user_id", nullable = false)
    @JsonIgnoreProperties({ "role", "userInfo", "directManager", "subordinates" })
    private User delegateUser;

    @Column(nullable = false)
    private Instant validFrom;

    @Column(nullable = false)
    private Instant validTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalDelegationStatus status = ApprovalDelegationStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(length = 50)
    private String scopeType = "ALL";

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
        if (this.status == null) {
            this.status = ApprovalDelegationStatus.ACTIVE;
        }
        if (this.scopeType == null) {
            this.scopeType = "ALL";
        }
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
