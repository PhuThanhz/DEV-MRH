package vn.system.app.modules.sectionjobtitle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.system.app.modules.sectionjobtitle.domain.SectionJobTitle;

import java.util.List;

@Repository
public interface SectionJobTitleRepository
        extends JpaRepository<SectionJobTitle, Long>, JpaSpecificationExecutor<SectionJobTitle> {

    // Kiểm tra trùng
    boolean existsByJobTitle_IdAndSection_Id(Long jobTitleId, Long sectionId);

    // ⭐ THÊM DÒNG NÀY ĐỂ FIX LỖI CỦA PHÚ
    SectionJobTitle findByJobTitle_IdAndSection_Id(Long jobTitleId, Long sectionId);

    // Section → Job Titles Active
    List<SectionJobTitle> findBySection_IdAndActiveTrue(Long sectionId);

    // Department → Job Titles Active (Section-based)
    List<SectionJobTitle> findBySection_Department_IdAndActiveTrue(Long departmentId);

    // ⭐ NEW — Kiểm tra job title còn đang active trong department hay không
    boolean existsBySection_Department_IdAndJobTitle_IdAndActiveTrue(Long departmentId, Long jobTitleId);

    // ⭐ NEW — Lấy tất cả SectionJobTitle ACTIVE theo JobTitle
    List<SectionJobTitle> findByJobTitle_IdAndActiveTrue(Long jobTitleId);

    // ⭐ NEW — Lấy tất cả SectionJobTitle ACTIVE theo JobTitle & Department
    List<SectionJobTitle> findByJobTitle_IdAndSection_Department_IdAndActiveTrue(Long jobTitleId, Long departmentId);

    // ⭐ NEW — Lấy tất cả SectionJobTitle ACTIVE
    List<SectionJobTitle> findByActiveTrue();
}