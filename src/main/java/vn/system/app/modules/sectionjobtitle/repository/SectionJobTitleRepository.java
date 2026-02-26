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

        // FULL ENTITY
        SectionJobTitle findByJobTitle_IdAndSection_Id(Long jobTitleId, Long sectionId);

        // Section → Job Titles Active
        List<SectionJobTitle> findBySection_IdAndActiveTrue(Long sectionId);

        // Department → Job Titles Active (Section-based)
        List<SectionJobTitle> findBySection_Department_IdAndActiveTrue(Long departmentId);

        // NEW — kiểm tra active trong Department
        boolean existsBySection_Department_IdAndJobTitle_IdAndActiveTrue(Long departmentId, Long jobTitleId);

        // NEW — ACTIVE by jobTitle
        List<SectionJobTitle> findByJobTitle_IdAndActiveTrue(Long jobTitleId);

        // NEW — ACTIVE by jobTitle & department
        List<SectionJobTitle> findByJobTitle_IdAndSection_Department_IdAndActiveTrue(
                        Long jobTitleId, Long departmentId);

        // ALL ACTIVE
        List<SectionJobTitle> findByActiveTrue();

        // COMPANY GET
        List<SectionJobTitle> findBySection_Department_Company_IdAndActiveTrue(Long companyId);

        // NEW — kiểm tra jobTitle active trong toàn công ty
        boolean existsBySection_Department_Company_IdAndJobTitle_IdAndActiveTrue(
                        Long companyId,
                        Long jobTitleId);

        // ⭐⭐ NEW — hàm cần thiết cho PositionService
        boolean existsBySection_IdAndJobTitle_IdAndActiveTrue(Long sectionId, Long jobTitleId);
}