package vn.system.app.modules.jobtitle.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.jobtitle.domain.JobTitle;

@Repository
public interface JobTitleRepository
        extends JpaRepository<JobTitle, Long>, JpaSpecificationExecutor<JobTitle> {

    boolean existsByNameVi(String nameVi);

    // ★ Thêm mới: để lấy tất cả chức danh đang active (phục vụ
    // CompanyJobTitleService)
    List<JobTitle> findByActiveTrue();

    // PHỤC VỤ LỘ TRÌNH THĂNG TIẾN (CAO → THẤP)
    List<JobTitle> findAllByOrderByPositionLevel_BandOrderDesc();
}