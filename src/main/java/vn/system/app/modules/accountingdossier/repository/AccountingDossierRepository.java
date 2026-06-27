package vn.system.app.modules.accountingdossier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.accountingdossier.domain.AccountingDossier;

@Repository
public interface AccountingDossierRepository extends
        JpaRepository<AccountingDossier, Long>,
        JpaSpecificationExecutor<AccountingDossier> {

    boolean existsByDossierCode(String dossierCode);
}
