package vn.system.app.modules.accountingdossier.domain;

import java.time.Instant;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.accountingdossier.domain.enums.WorkflowInstanceStatus;

@Entity
@Table(name = "accounting_approval_instances", indexes = {
        @Index(name = "idx_acc_wf_instance_dossier", columnList = "dossier_id,submission_no"),
        @Index(name = "idx_acc_wf_instance_status", columnList = "status")
})
@Getter
@Setter
public class AccountingApprovalInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dossier_id", nullable = false)
    private AccountingDossier dossier;

    @Column(name = "submission_no", nullable = false)
    private Integer submissionNo;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "template_version")
    private Integer templateVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WorkflowInstanceStatus status = WorkflowInstanceStatus.ACTIVE;

    @Column(name = "snapshot_json", columnDefinition = "TEXT")
    private String snapshotJson;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_by")
    private String createdBy;

    @PrePersist
    public void beforeCreate() {
        this.startedAt = this.startedAt == null ? Instant.now() : this.startedAt;
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
        if (this.status == null) {
            this.status = WorkflowInstanceStatus.ACTIVE;
        }
    }
}
