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

import org.springframework.data.repository.query.Param;

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

    boolean existsByDossierCategory_Id(Long dossierCategoryId);

    List<AccountingDossier> findByActiveTrueAndStorageStatusAndRetentionUntilBefore(
            AccountingDossierStorageStatus storageStatus,
            Instant retentionUntil);

    long countByActiveTrue();
    long countByActiveTrueAndCompanyId(Long companyId);

    long countByActiveTrueAndStorageStatus(AccountingDossierStorageStatus storageStatus);
    long countByActiveTrueAndStorageStatusAndCompanyId(AccountingDossierStorageStatus storageStatus, Long companyId);

    long countByActiveTrueAndRetentionUntilBetween(Instant from, Instant to);
    long countByActiveTrueAndRetentionUntilBetweenAndCompanyId(Instant from, Instant to, Long companyId);

    @Query("SELECT d.status, COUNT(d) FROM AccountingDossier d WHERE d.active = true AND (:companyId IS NULL OR d.company.id = :companyId) GROUP BY d.status")
    List<Object[]> countActiveGroupByStatus(@Param("companyId") Long companyId);

    @Query("SELECT d.storageStatus, COUNT(d) FROM AccountingDossier d WHERE d.active = true AND (:companyId IS NULL OR d.company.id = :companyId) GROUP BY d.storageStatus")
    List<Object[]> countActiveGroupByStorageStatus(@Param("companyId") Long companyId);

    @Query(value = "SELECT COUNT(*), " +
            "SUM(CASE WHEN storage_status = 'IN_RETENTION' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN storage_status = 'EXPIRED' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN storage_status = 'ARCHIVED' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN retention_until BETWEEN :from AND :to THEN 1 ELSE 0 END) " +
            "FROM accounting_dossier WHERE active = true AND (:companyId IS NULL OR company_id = :companyId)", nativeQuery = true)
    Object[] getStorageSummaryCounts(@Param("companyId") Long companyId, @Param("from") Instant from, @Param("to") Instant to);

    @Query(value = "SELECT 'STATUS', status, COUNT(*) FROM accounting_dossier WHERE active = true AND (:companyId IS NULL OR company_id = :companyId) GROUP BY status " +
            "UNION ALL SELECT 'STORAGE', storage_status, COUNT(*) FROM accounting_dossier WHERE active = true AND (:companyId IS NULL OR company_id = :companyId) GROUP BY storage_status", nativeQuery = true)
    List<Object[]> getStorageSummaryGroups(@Param("companyId") Long companyId);

    @Query("SELECT d.department.id, d.department.name, COUNT(d) " +
            "FROM AccountingDossier d WHERE d.active = true AND (:companyId IS NULL OR d.company.id = :companyId) GROUP BY d.department.id, d.department.name")
    List<Object[]> countActiveGroupByDepartment(@Param("companyId") Long companyId);

    @Query("SELECT d.dossierCategory.id, d.dossierCategory.categoryName, d.customCategoryName, COUNT(d) " +
            "FROM AccountingDossier d WHERE d.active = true AND (:companyId IS NULL OR d.company.id = :companyId) " +
            "GROUP BY d.dossierCategory.id, d.dossierCategory.categoryName, d.customCategoryName")
    List<Object[]> countActiveGroupByCategory(@Param("companyId") Long companyId);
}
