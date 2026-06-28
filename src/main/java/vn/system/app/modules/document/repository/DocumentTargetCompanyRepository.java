package vn.system.app.modules.document.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.document.domain.DocumentTargetCompany;

@Repository
public interface DocumentTargetCompanyRepository extends JpaRepository<DocumentTargetCompany, Long>, JpaSpecificationExecutor<DocumentTargetCompany> {

    List<DocumentTargetCompany> findByDocument_Id(Long documentId);

    List<DocumentTargetCompany> findByDocument_IdIn(Collection<Long> documentIds);

    void deleteByDocument_Id(Long documentId);

    boolean existsByDocument_IdAndCompanyIdIn(Long documentId, Collection<Long> companyIds);
}
