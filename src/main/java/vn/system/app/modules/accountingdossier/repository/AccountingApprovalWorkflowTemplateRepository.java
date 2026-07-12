package vn.system.app.modules.accountingdossier.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import vn.system.app.modules.accountingdossier.domain.AccountingApprovalWorkflowTemplate;
import vn.system.app.modules.accountingdossier.domain.enums.WorkflowTemplateStatus;

@Repository
public interface AccountingApprovalWorkflowTemplateRepository
        extends JpaRepository<AccountingApprovalWorkflowTemplate, Long> {

    boolean existsByCodeAndCompanyIdAndVersion(String code, Long companyId, Integer version);

    boolean existsByCodeAndCompanyIdAndVersionAndIdNot(String code, Long companyId, Integer version, Long id);

    AccountingApprovalWorkflowTemplate findTopByCodeAndCompanyIdOrderByVersionDesc(String code, Long companyId);

    List<AccountingApprovalWorkflowTemplate> findByStatusAndCompanyIdInOrderByPriorityAscIdAsc(
            WorkflowTemplateStatus status,
            List<Long> companyIds);

    List<AccountingApprovalWorkflowTemplate> findByStatusOrderByPriorityAscIdAsc(WorkflowTemplateStatus status);

    @Query("select distinct template from AccountingApprovalWorkflowTemplate template left join fetch template.scopes " +
           "where template.status = :status and (template.companyId is null or template.companyId = :companyId) " +
           "and (template.dossierCategoryId is null or template.dossierCategoryId = :categoryId) " +
           "and (template.effectiveFrom is null or template.effectiveFrom <= :now) " +
           "and (template.effectiveTo is null or template.effectiveTo > :now) order by template.priority asc, template.id asc")
    List<AccountingApprovalWorkflowTemplate> findActiveCandidatesWithScopes(@Param("status") WorkflowTemplateStatus status,
            @Param("companyId") Long companyId, @Param("categoryId") Long categoryId, @Param("now") Instant now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select template from AccountingApprovalWorkflowTemplate template where template.status = :status")
    List<AccountingApprovalWorkflowTemplate> findByStatusForUpdate(@Param("status") WorkflowTemplateStatus status);
}
