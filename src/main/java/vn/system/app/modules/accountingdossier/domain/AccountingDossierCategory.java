package vn.system.app.modules.accountingdossier.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.document.domain.AccountingDocumentCategory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounting_dossier_category")
@Getter
@Setter
public class AccountingDossierCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String categoryCode;
    
    private String categoryName;
    
    @Column(columnDefinition = "TEXT")
    private String description;

    private Long companyId;

    private String scope; // COMPANY, GLOBAL
    
    private String source; // MANUAL, SYNCED_FROM_UNSTRUCTURED

    @Column(nullable = false)
    private Integer version = 1;

    private boolean active = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "accounting_dossier_category_documents",
        joinColumns = @JoinColumn(name = "dossier_category_id"),
        inverseJoinColumns = @JoinColumn(name = "document_category_id")
    )
    private List<AccountingDocumentCategory> documentCategories = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
        this.active = true;
        if (this.version == null) {
            this.version = 1;
        }
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
