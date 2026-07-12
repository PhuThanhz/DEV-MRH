package vn.system.app.modules.accountingdossier.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.accountingdossier.domain.AccountingApprovalInstance;
import vn.system.app.modules.accountingdossier.domain.enums.WorkflowInstanceStatus;

@Repository
public interface AccountingApprovalInstanceRepository extends JpaRepository<AccountingApprovalInstance, Long> {

    Optional<AccountingApprovalInstance> findFirstByDossierIdAndStatusOrderByIdDesc(
            Long dossierId,
            WorkflowInstanceStatus status);

    long countByDossierId(Long dossierId);
}
