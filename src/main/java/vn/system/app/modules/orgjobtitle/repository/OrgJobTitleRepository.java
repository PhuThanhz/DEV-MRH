package vn.system.app.modules.orgjobtitle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.orgjobtitle.domain.OrgJobTitle;

@Repository
public interface OrgJobTitleRepository
        extends JpaRepository<OrgJobTitle, Long>, JpaSpecificationExecutor<OrgJobTitle> {

    // ===== CHECK TRÙNG =====

    boolean existsByJobTitle_IdAndCompany_Id(Long jobTitleId, Long companyId);

    boolean existsByJobTitle_IdAndDepartment_Id(Long jobTitleId, Long departmentId);

    boolean existsByJobTitle_IdAndSection_Id(Long jobTitleId, Long sectionId);

    // ===== LẤY THEO ORG =====

    OrgJobTitle findByJobTitle_IdAndCompany_Id(Long jobTitleId, Long companyId);

    OrgJobTitle findByJobTitle_IdAndDepartment_Id(Long jobTitleId, Long departmentId);

    OrgJobTitle findByJobTitle_IdAndSection_Id(Long jobTitleId, Long sectionId);
}
