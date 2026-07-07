package vn.system.app.modules.accountingdossier.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.document.domain.AccountingDocumentCategory;

@Entity
@Table(name = "accounting_dossier_category_documents")
@Getter
@Setter
public class AccountingDossierCategoryDocument {

    @EmbeddedId
    private AccountingDossierCategoryDocumentId id = new AccountingDossierCategoryDocumentId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("dossierCategoryId")
    @JoinColumn(name = "dossier_category_id")
    private AccountingDossierCategory dossierCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("documentCategoryId")
    @JoinColumn(name = "document_category_id")
    private AccountingDocumentCategory documentCategory;

    @Column(name = "is_required", nullable = false)
    private boolean required = true;

    @Column(name = "sort_order")
    private Integer sortOrder;

    private Instant createdAt;
    private String createdBy;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
