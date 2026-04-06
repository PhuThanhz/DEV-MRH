package vn.system.app.modules.companyjobtitle.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.companyjobtitle.domain.CompanyJobTitle;

@Repository
public interface CompanyJobTitleRepository
                extends JpaRepository<CompanyJobTitle, Long>,
                JpaSpecificationExecutor<CompanyJobTitle> {

        CompanyJobTitle findByCompany_IdAndJobTitle_Id(Long companyId, Long jobTitleId);

        boolean existsByCompany_IdAndJobTitle_IdAndActiveTrue(Long companyId, Long jobTitleId);

        List<CompanyJobTitle> findByCompany_IdAndActiveTrue(Long companyId);

        Optional<CompanyJobTitle> findByCompany_IdAndJobTitle_IdAndActiveTrue(
                        Long companyId,
                        Long jobTitleId);

        List<CompanyJobTitle> findByJobTitle_IdAndActiveTrue(Long jobTitleId);

        List<CompanyJobTitle> findByCompany_IdIn(List<Long> companyIds);
}