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

    /**
     * Tìm mapping theo company + jobTitle (bất kể active hay không)
     */
    CompanyJobTitle findByCompany_IdAndJobTitle_Id(Long companyId, Long jobTitleId);

    /**
     * Kiểm tra jobTitle có đang active ở cấp công ty không
     * (dùng để validate khi gán ở Department / Section)
     */
    boolean existsByCompany_IdAndJobTitle_IdAndActiveTrue(Long companyId, Long jobTitleId);

    /**
     * Lấy tất cả mapping active của một công ty
     * (dùng cho hiển thị tổng hợp)
     */
    List<CompanyJobTitle> findByCompany_IdAndActiveTrue(Long companyId);

    /**
     * Lấy mapping active (nếu cần dùng khi restore hoặc auto-deactivate)
     */
    Optional<CompanyJobTitle> findByCompany_IdAndJobTitle_IdAndActiveTrue(
            Long companyId,
            Long jobTitleId);

    List<CompanyJobTitle> findByJobTitle_IdAndActiveTrue(Long jobTitleId);

}
