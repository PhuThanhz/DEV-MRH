package vn.system.app.modules.document.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.document.domain.AccountingDocumentCategory;

@Repository
public interface AccountingDocumentCategoryRepository extends
        JpaRepository<AccountingDocumentCategory, Long>,
        JpaSpecificationExecutor<AccountingDocumentCategory> {

    boolean existsByCategoryCode(String categoryCode);

    boolean existsByCategoryCodeAndIdNot(String categoryCode, Long id);

    Optional<AccountingDocumentCategory> findByCategoryCode(String categoryCode);

    List<AccountingDocumentCategory> findByActiveTrueOrderByCategoryNameAsc();
}
