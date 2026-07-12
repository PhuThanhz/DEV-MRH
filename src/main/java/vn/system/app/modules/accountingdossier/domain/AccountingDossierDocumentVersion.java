package vn.system.app.modules.accountingdossier.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "accounting_dossier_document_version", indexes = {
        @Index(name = "idx_acc_doc_version_document", columnList = "dossier_document_id,version_no")
})
@Getter
@Setter
public class AccountingDossierDocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_document_id", nullable = false)
    private AccountingDossierDocument dossierDocument;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "file_url", length = 1000)
    private String fileUrl;

    @Column(name = "external_link", length = 1000)
    private String externalLink;

    @Column(name = "change_note", columnDefinition = "TEXT")
    private String changeNote;

    private Instant createdAt;
    private String createdBy;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
