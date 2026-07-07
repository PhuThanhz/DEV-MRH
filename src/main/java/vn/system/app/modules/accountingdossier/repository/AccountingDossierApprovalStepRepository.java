package vn.system.app.modules.accountingdossier.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.system.app.modules.accountingdossier.domain.AccountingDossierApprovalStep;

@Repository
public interface AccountingDossierApprovalStepRepository extends JpaRepository<AccountingDossierApprovalStep, Long> {

    List<AccountingDossierApprovalStep> findByDossierIdAndActiveTrueOrderByStepOrderAsc(Long dossierId);

    Optional<AccountingDossierApprovalStep> findByDossierIdAndStatusAndActiveTrue(Long dossierId, String status);

    List<AccountingDossierApprovalStep> findByDossierIdAndActiveTrue(Long dossierId);

    /**
     * Tìm tất cả dossierId mà user (userId) đang là người duyệt ở bước CURRENT.
     * Điều kiện: step.status = CURRENT, step.active = true và
     *   (step.approverUserId = :userId)  -- được chỉ định trực tiếp
     * hoặc (step.approverUserId IS NULL AND step.approverType IN :approverTypes) -- theo role
     */
    @Query("SELECT DISTINCT s.dossier.id FROM AccountingDossierApprovalStep s " +
           "WHERE s.status = 'CURRENT' AND s.active = true " +
           "AND (s.approverUserId = :userId " +
           "     OR (s.approverUserId IS NULL AND s.approverType IN :approverTypes))")
    List<Long> findDossierIdsPendingForApprover(@Param("userId") String userId,
                                                @Param("approverTypes") List<String> approverTypes);

    @Query("SELECT s.approverType, COUNT(s) FROM AccountingDossierApprovalStep s " +
           "WHERE s.status = 'CURRENT' AND s.active = true GROUP BY s.approverType")
    List<Object[]> countCurrentStepsGroupByApproverType();
}
