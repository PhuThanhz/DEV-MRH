package vn.system.app.modules.accountingdossier.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.document.domain.AccountingDocumentCategory;
import vn.system.app.modules.document.domain.Document;

import java.time.Instant;
import java.math.BigDecimal;

@Entity
@Table(name = "accounting_dossier_document")
@Getter
@Setter
public class AccountingDossierDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false)
    private AccountingDossier dossier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accounting_category_id", nullable = false)
    private AccountingDocumentCategory accountingCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @Column(name = "document_name", nullable = false, length = 255)
    private String documentName;

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType; // PDF, EXCEL, WORD, IMAGE, OTHER

    @Column(name = "check_status", nullable = false, length = 50)
    private String checkStatus = "PENDING"; // PENDING, VALID, NEED_SUPPLEMENT, INVALID, NOT_REQUIRED

    @Column(name = "check_note", columnDefinition = "TEXT")
    private String checkNote;

    @Column(name = "file_url", length = 1000)
    private String fileUrl;

    @Column(name = "external_link", length = 1000)
    private String externalLink;

    @Column(name = "invoice_date")
    private Instant invoiceDate;

    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    @Column(name = "invoice_content", length = 500)
    private String invoiceContent;

    @Column(name = "partner_name", length = 255)
    private String partnerName;

    @Column(name = "partner_type", length = 50)
    private String partnerType;

    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
        this.active = true;
        if (this.checkStatus == null) {
            this.checkStatus = "PENDING";
        }
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
