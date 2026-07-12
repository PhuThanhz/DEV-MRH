package vn.system.app.modules.accountingdossier.domain;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Basic;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.accountingdossier.domain.enums.AccountingDossierCategoryMode;
import vn.system.app.modules.accountingdossier.domain.enums.AccountingDossierStatus;
import vn.system.app.modules.accountingdossier.domain.enums.AccountingDossierStorageStatus;
import vn.system.app.modules.company.domain.Company;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.section.domain.Section;

@Entity
@Table(name = "accounting_dossier", indexes = {
    @Index(name = "idx_dossier_company", columnList = "company_id"),
    @Index(name = "idx_dossier_department", columnList = "department_id"),
    @Index(name = "idx_dossier_status", columnList = "status"),
    @Index(name = "idx_dossier_storage_status", columnList = "storage_status"),
    @Index(name = "idx_dossier_active", columnList = "active"),
    @Index(name = "idx_dossier_retention_until", columnList = "retention_until"),
    @Index(name = "idx_dossier_created_at", columnList = "created_at")
})
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class AccountingDossier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dossier_code", length = 100, unique = true)
    private String dossierCode;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_mode", length = 30)
    private AccountingDossierCategoryMode categoryMode;

    @Column(name = "custom_category_name", length = 255)
    private String customCategoryName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_category_id")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "documentCategories" })
    private AccountingDossierCategory dossierCategory;

    @Column(name = "dossier_category_version")
    private Integer dossierCategoryVersion;

    @Column(name = "sync_category_requested", nullable = false)
    private boolean syncCategoryRequested = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "company" })
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "department" })
    private Section section;

    @Column(name = "creator_id", length = 36)
    private String creatorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private AccountingDossierStatus status = AccountingDossierStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_status", nullable = false, length = 40)
    private AccountingDossierStorageStatus storageStatus = AccountingDossierStorageStatus.IN_RETENTION;

    @Column(name = "retention_years", nullable = false)
    private Integer retentionYears = 10;

    @Column(name = "retention_until")
    private Instant retentionUntil;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "terminated_at")
    private Instant terminatedAt;

    @Column(name = "return_count", nullable = false)
    private Integer returnCount = 0;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "qr_token", unique = true, length = 64)
    private String qrToken;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "qr_code", columnDefinition = "MEDIUMTEXT")
    private String qrCode;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    @Version
    private Long version;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
        this.creatorId = this.creatorId == null
                ? SecurityUtil.getCurrentUserId().orElse(this.createdBy)
                : this.creatorId;
        this.active = true;
        if (this.status == null) {
            this.status = AccountingDossierStatus.DRAFT;
        }
        if (this.storageStatus == null) {
            this.storageStatus = AccountingDossierStorageStatus.IN_RETENTION;
        }
        if (this.retentionYears == null) {
            this.retentionYears = 10;
        }
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
