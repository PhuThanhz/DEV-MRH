package vn.system.app.modules.accountingdossier.domain;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class AccountingDossierCategoryDocumentId implements Serializable {

    @Column(name = "dossier_category_id")
    private Long dossierCategoryId;

    @Column(name = "document_category_id")
    private Long documentCategoryId;

    public AccountingDossierCategoryDocumentId(Long dossierCategoryId, Long documentCategoryId) {
        this.dossierCategoryId = dossierCategoryId;
        this.documentCategoryId = documentCategoryId;
    }
}
