package vn.system.app.modules.accountingdossier.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import vn.system.app.modules.accountingdossier.domain.AccountingDossierApprovalStep;
import vn.system.app.modules.accountingdossier.domain.enums.ApproverType;
import vn.system.app.modules.accountingdossier.domain.enums.ApprovalStepStatus;
import vn.system.app.modules.accountingdossier.domain.enums.AccountingDossierStorageStatus;

@Repository
public interface AccountingDossierApprovalStepRepository extends JpaRepository<AccountingDossierApprovalStep, Long> {

    List<AccountingDossierApprovalStep> findByDossierIdAndActiveTrueOrderByStepOrderAsc(Long dossierId);

    Optional<AccountingDossierApprovalStep> findByDossierIdAndStatusAndActiveTrue(Long dossierId, ApprovalStepStatus status);

    List<AccountingDossierApprovalStep> findByDossierIdAndActiveTrue(Long dossierId);

    /**
     * Tìm tất cả dossierId mà user (userId) đang là người duyệt ở bước CURRENT.
     * Điều kiện: step.status = CURRENT, step.active = true và
     *   (step.approverUserId = :userId)  -- được chỉ định trực tiếp
     * hoặc (step.approverUserId IS NULL AND step.approverType IN :approverTypes) -- theo role
     */
    @Query("SELECT DISTINCT s.dossier.id FROM AccountingDossierApprovalStep s " +
           "WHERE s.status = vn.system.app.modules.accountingdossier.domain.enums.ApprovalStepStatus.CURRENT AND s.active = true " +
           "AND (s.approverUserId = :userId " +
           "     OR LOCATE(:userId, COALESCE(s.eligibleApproverIds, '')) > 0 " +
           "     OR (s.approverUserId IS NULL AND s.approverType IN :approverTypes))")
    List<Long> findDossierIdsPendingForApprover(@Param("userId") String userId,
                                                @Param("approverTypes") List<ApproverType> approverTypes);

    @Query("SELECT s.dossier FROM AccountingDossierApprovalStep s " +
           "WHERE s.status = vn.system.app.modules.accountingdossier.domain.enums.ApprovalStepStatus.CURRENT " +
           "AND s.active = true AND s.dossier.active = true " +
           "AND (s.approverUserId = :userId " +
           "     OR LOCATE(:userId, COALESCE(s.eligibleApproverIds, '')) > 0) " +
           "ORDER BY s.createdAt DESC, s.id DESC")
    Page<vn.system.app.modules.accountingdossier.domain.AccountingDossier> findCurrentDossiersForDirectApprover(
            @Param("userId") String userId,
            Pageable pageable);

    @Query("SELECT s.dossier FROM AccountingDossierApprovalStep s " +
           "WHERE s.status = vn.system.app.modules.accountingdossier.domain.enums.ApprovalStepStatus.CURRENT " +
           "AND s.active = true AND s.dossier.active = true " +
           "AND (s.approverUserId = :userId " +
           "     OR LOCATE(:userId, COALESCE(s.eligibleApproverIds, '')) > 0 " +
           "     OR (s.approverUserId IS NULL AND s.approverType IN :approverTypes)) " +
           "ORDER BY s.createdAt DESC, s.id DESC")
    Page<vn.system.app.modules.accountingdossier.domain.AccountingDossier> findCurrentDossiersForApprover(
            @Param("userId") String userId,
            @Param("approverTypes") List<ApproverType> approverTypes,
            Pageable pageable);

    @Query("SELECT s.approverType, COUNT(s) FROM AccountingDossierApprovalStep s " +
           "WHERE s.status = vn.system.app.modules.accountingdossier.domain.enums.ApprovalStepStatus.CURRENT AND s.active = true " +
           "AND (:companyId IS NULL OR s.dossier.company.id = :companyId) GROUP BY s.approverType")
    List<Object[]> countCurrentStepsGroupByApproverType(@Param("companyId") Long companyId);

    @Query("SELECT s.dossier FROM AccountingDossierApprovalStep s " +
           "WHERE s.active = true AND s.dossier.active = true " +
           "AND s.approverUserId = :userId " +
           "AND (:storageStatus IS NULL OR s.dossier.storageStatus = :storageStatus) " +
           "AND (:companyId IS NULL OR s.dossier.company.id = :companyId) " +
           "AND (:departmentId IS NULL OR s.dossier.department.id = :departmentId) " +
           "AND (:dossierCategoryId IS NULL OR s.dossier.dossierCategory.id = :dossierCategoryId) " +
           "AND s.status IN (vn.system.app.modules.accountingdossier.domain.enums.ApprovalStepStatus.APPROVED, " +
           "vn.system.app.modules.accountingdossier.domain.enums.ApprovalStepStatus.REJECTED, " +
           "vn.system.app.modules.accountingdossier.domain.enums.ApprovalStepStatus.RETURNED, " +
           "vn.system.app.modules.accountingdossier.domain.enums.ApprovalStepStatus.CANCELLED) " +
           "ORDER BY s.actedAt DESC, s.id DESC")
    Page<vn.system.app.modules.accountingdossier.domain.AccountingDossier> findProcessedDossiersByApprover(
            @Param("userId") String userId,
            @Param("storageStatus") AccountingDossierStorageStatus storageStatus,
            @Param("companyId") Long companyId,
            @Param("departmentId") Long departmentId,
            @Param("dossierCategoryId") Long dossierCategoryId,
            Pageable pageable);

    List<AccountingDossierApprovalStep> findByStatusAndActiveTrueAndDueAtBefore(
            ApprovalStepStatus status,
            Instant dueAt);
}
