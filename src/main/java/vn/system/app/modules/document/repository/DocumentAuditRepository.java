package vn.system.app.modules.document.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.document.domain.DocumentAudit;
import java.util.List;

@Repository
public interface DocumentAuditRepository extends JpaRepository<DocumentAudit, Long>, JpaSpecificationExecutor<DocumentAudit> {
    List<DocumentAudit> findByDocumentIdOrderByCreatedAtDesc(Long documentId);
}
