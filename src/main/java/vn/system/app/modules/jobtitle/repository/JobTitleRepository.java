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

    // ★ CREATE: kiểm tra trùng nameVi + cùng bậc (positionLevel)
    // Dùng trong handleCreate — khác công ty nhưng khác bậc → cho phép
    boolean existsByNameViAndPositionLevel_Id(String nameVi, Long positionLevelId);

    // ★ UPDATE: kiểm tra trùng nameVi + cùng bậc, nhưng bỏ qua chính record đang
    // sửa
    // Dùng trong handleUpdate — tránh báo lỗi khi user chỉ sửa field khác mà không
    // đổi tên
    boolean existsByNameViAndPositionLevel_IdAndIdNot(String nameVi, Long positionLevelId, Long id);
}