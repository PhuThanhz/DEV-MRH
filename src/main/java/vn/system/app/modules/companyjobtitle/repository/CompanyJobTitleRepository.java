package vn.system.app.modules.companyjobtitle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.companyjobtitle.domain.CompanyJobTitle;

@Repository
public interface CompanyJobTitleRepository
        extends JpaRepository<CompanyJobTitle, Long>,
        JpaSpecificationExecutor<CompanyJobTitle> {

    boolean existsByJobTitle_IdAndCompany_Id(Long jobTitleId, Long companyId);
}
