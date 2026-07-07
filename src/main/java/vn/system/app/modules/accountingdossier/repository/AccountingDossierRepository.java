package vn.system.app.modules.accountingdossier.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.accountingdossier.domain.AccountingDossier;
import vn.system.app.modules.accountingdossier.domain.enums.AccountingDossierStorageStatus;

@Repository
public interface AccountingDossierRepository extends
        JpaRepository<AccountingDossier, Long>,
        JpaSpecificationExecutor<AccountingDossier> {

    @Override
    @EntityGraph(attributePaths = {"company", "department", "section", "dossierCategory"})
    Page<AccountingDossier> findAll(Specification<AccountingDossier> spec, Pageable pageable);

    Optional<AccountingDossier> findByQrToken(String qrToken);

    boolean existsByDossierCode(String dossierCode);

    List<AccountingDossier> findByActiveTrueAndStorageStatusAndRetentionUntilBefore(
            AccountingDossierStorageStatus storageStatus,
            Instant retentionUntil);

    long countByActiveTrue();

    long countByActiveTrueAndStorageStatus(AccountingDossierStorageStatus storageStatus);

    long countByActiveTrueAndRetentionUntilBetween(Instant from, Instant to);

    @Query("SELECT d.status, COUNT(d) FROM AccountingDossier d WHERE d.active = true GROUP BY d.status")
    List<Object[]> countActiveGroupByStatus();

    @Query("SELECT d.storageStatus, COUNT(d) FROM AccountingDossier d WHERE d.active = true GROUP BY d.storageStatus")
    List<Object[]> countActiveGroupByStorageStatus();

    @Query("SELECT d.department.id, d.department.name, COUNT(d) " +
            "FROM AccountingDossier d WHERE d.active = true GROUP BY d.department.id, d.department.name")
    List<Object[]> countActiveGroupByDepartment();

    @Query("SELECT d.dossierCategory.id, d.dossierCategory.categoryName, d.customCategoryName, COUNT(d) " +
            "FROM AccountingDossier d WHERE d.active = true " +
            "GROUP BY d.dossierCategory.id, d.dossierCategory.categoryName, d.customCategoryName")
    List<Object[]> countActiveGroupByCategory();
}
