package vn.system.app.modules.departmentjobtitle.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;
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

        @EntityGraph(attributePaths = { "jobTitle", "jobTitle.positionLevel", "department" })
        List<DepartmentJobTitle> findByDepartment_IdAndActiveTrue(Long departmentId);

        @EntityGraph(attributePaths = { "jobTitle", "jobTitle.positionLevel", "department" })
        List<DepartmentJobTitle> findByDepartment_Company_IdAndActiveTrue(Long companyId);

        boolean existsByDepartment_Company_IdAndJobTitle_IdAndActiveTrue(
                        Long companyId, Long jobTitleId);

        @EntityGraph(attributePaths = { "jobTitle", "jobTitle.positionLevel", "department" })
        List<DepartmentJobTitle> findByJobTitle_IdAndActiveTrue(Long jobTitleId);

        @EntityGraph(attributePaths = { "jobTitle", "jobTitle.positionLevel", "department" })
        Optional<DepartmentJobTitle> findByDepartment_IdAndJobTitle_IdAndActiveTrue(
                        Long departmentId, Long jobTitleId);

        List<DepartmentJobTitle> findByDepartment_IdIn(List<Long> departmentIds);

        @EntityGraph(attributePaths = { "jobTitle", "jobTitle.positionLevel", "department" })
        List<DepartmentJobTitle> findByDepartment_IdAndJobTitle_PositionLevel_CodeAndActiveTrue(
                        Long departmentId, String positionLevelCode);

        // lấy jobTitleId đã gán active trong phòng ban
        @Query("SELECT d.jobTitle.id FROM DepartmentJobTitle d " +
                        "WHERE d.department.id = :deptId AND d.active = true")
        List<Long> findActiveJobTitleIdsByDepartment(@Param("deptId") Long departmentId);

        // lấy tất cả active (thay thế findAll().stream().filter)
        @EntityGraph(attributePaths = { "jobTitle", "jobTitle.positionLevel", "department" })
        List<DepartmentJobTitle> findByActiveTrue();

        @EntityGraph(attributePaths = { "jobTitle", "jobTitle.positionLevel", "department" })
        @Override
        Page<DepartmentJobTitle> findAll(Specification<DepartmentJobTitle> spec, Pageable pageable);

        // ✅ MỚI — lấy tất cả active trong company, trừ phòng ban hiện tại
        // dùng để build usedInDepartments map
        @EntityGraph(attributePaths = { "jobTitle", "jobTitle.positionLevel", "department" })
        @Query("SELECT d FROM DepartmentJobTitle d " +
                        "WHERE d.department.company.id = :companyId " +
                        "AND d.department.id <> :excludeDeptId " +
                        "AND d.active = true")
        List<DepartmentJobTitle> findAllActiveByCompanyIdExcludingDepartment(
                        @Param("companyId") Long companyId,
                        @Param("excludeDeptId") Long excludeDeptId);

        @Query("SELECT DISTINCT d.department.id FROM DepartmentJobTitle d WHERE d.department.id IN :departmentIds AND d.active = TRUE")
        java.util.Set<Long> findDepartmentIdsWithJobTitleMap(@Param("departmentIds") java.util.Collection<Long> departmentIds);
}
