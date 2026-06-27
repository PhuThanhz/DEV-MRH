package vn.system.app.modules.accountingdossier.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.accountingdossier.domain.AccountingDossierCategory;

@Repository
public interface AccountingDossierCategoryRepository extends JpaRepository<AccountingDossierCategory, Long>,
        JpaSpecificationExecutor<AccountingDossierCategory> {

    Optional<AccountingDossierCategory> findByCategoryCode(String categoryCode);

    List<AccountingDossierCategory> findByActiveTrueOrderByCategoryNameAsc();
}
