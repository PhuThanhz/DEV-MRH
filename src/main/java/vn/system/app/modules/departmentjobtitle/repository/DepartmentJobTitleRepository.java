package vn.system.app.modules.departmentjobtitle.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.departmentjobtitle.domain.DepartmentJobTitle;

@Repository
public interface DepartmentJobTitleRepository
                extends JpaRepository<DepartmentJobTitle, Long>,
                JpaSpecificationExecutor<DepartmentJobTitle> {

        boolean existsByJobTitle_IdAndDepartment_Id(Long jobTitleId, Long departmentId);

        boolean existsByDepartment_IdAndJobTitle_Id(Long departmentId, Long jobTitleId);

        boolean existsByDepartment_IdAndJobTitle_IdAndActiveTrue(Long departmentId, Long jobTitleId);

        DepartmentJobTitle findByDepartment_IdAndJobTitle_Id(Long departmentId, Long jobTitleId);

        List<DepartmentJobTitle> findByDepartment_Id(Long departmentId);

        List<DepartmentJobTitle> findByDepartment_IdAndActiveTrue(Long departmentId);

        List<DepartmentJobTitle> findByDepartment_Company_IdAndActiveTrue(Long companyId);

        boolean existsByDepartment_Company_IdAndJobTitle_IdAndActiveTrue(
                        Long companyId, Long jobTitleId);

        List<DepartmentJobTitle> findByJobTitle_IdAndActiveTrue(Long jobTitleId);

        Optional<DepartmentJobTitle> findByDepartment_IdAndJobTitle_IdAndActiveTrue(
                        Long departmentId, Long jobTitleId);

        List<DepartmentJobTitle> findByDepartment_IdIn(List<Long> departmentIds);

        // ✅ THÊM — dùng cho fetchJobTitlesByLevel: lấy JobTitle đã gán vào phòng ban
        // theo level code
        List<DepartmentJobTitle> findByDepartment_IdAndJobTitle_PositionLevel_CodeAndActiveTrue(
                        Long departmentId, String positionLevelCode);
}