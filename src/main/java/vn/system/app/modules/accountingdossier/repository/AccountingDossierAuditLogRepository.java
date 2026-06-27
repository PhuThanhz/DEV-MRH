package vn.system.app.modules.accountingdossier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.accountingdossier.domain.AccountingDossierAuditLog;

import java.util.List;

@Repository
public interface AccountingDossierAuditLogRepository extends JpaRepository<AccountingDossierAuditLog, Long> {

    List<AccountingDossierAuditLog> findByDossierIdOrderByCreatedAtDesc(Long dossierId);

    void deleteByDossierId(Long dossierId);
}
