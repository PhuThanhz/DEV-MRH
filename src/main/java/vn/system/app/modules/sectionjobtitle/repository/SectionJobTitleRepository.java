package vn.system.app.modules.sectionjobtitle.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.sectionjobtitle.domain.SectionJobTitle;

@Repository
public interface SectionJobTitleRepository
        extends JpaRepository<SectionJobTitle, Long>,
        JpaSpecificationExecutor<SectionJobTitle> {

    // Kiểm tra trùng
    boolean existsByJobTitle_IdAndSection_Id(Long jobTitleId, Long sectionId);

    // ⭐ Fix logic cũ
    SectionJobTitle findByJobTitle_IdAndSection_Id(Long jobTitleId, Long sectionId);

    // Section → Job Titles Active
    List<SectionJobTitle> findBySection_IdAndActiveTrue(Long sectionId);

    // Department → Job Titles Active (Section-based)
    List<SectionJobTitle> findBySection_Department_IdAndActiveTrue(Long departmentId);

    // ⭐ NEW — Kiểm tra job title còn active trong department
    boolean existsBySection_Department_IdAndJobTitle_IdAndActiveTrue(Long departmentId, Long jobTitleId);

    // ⭐ NEW — Lấy SectionJobTitle ACTIVE theo JobTitle
    List<SectionJobTitle> findByJobTitle_IdAndActiveTrue(Long jobTitleId);

    // ⭐ NEW — Lấy SectionJobTitle ACTIVE theo JobTitle & Department
    List<SectionJobTitle> findByJobTitle_IdAndSection_Department_IdAndActiveTrue(
            Long jobTitleId, Long departmentId);

    // ⭐ NEW — Lấy tất cả SectionJobTitle ACTIVE
    List<SectionJobTitle> findByActiveTrue();

    // ⭐⭐ NEW — COMPANY GET (FIX LỖI)
    List<SectionJobTitle> findBySection_Department_Company_IdAndActiveTrue(Long companyId);

    // ⭐ NEW — kiểm tra JobTitle đã active ở bộ phận thuộc công ty hay chưa
    boolean existsBySection_Department_Company_IdAndJobTitle_IdAndActiveTrue(
            Long companyId,
            Long jobTitleId);

}
