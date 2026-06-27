package vn.system.app.modules.accountingdossier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.accountingdossier.domain.AccountingDossierDocument;

import java.util.List;

@Repository
public interface AccountingDossierDocumentRepository extends JpaRepository<AccountingDossierDocument, Long> {
    
    List<AccountingDossierDocument> findByDossierId(Long dossierId);
    
    long countByDossierId(Long dossierId);

    void deleteByDossierId(Long dossierId);
}
