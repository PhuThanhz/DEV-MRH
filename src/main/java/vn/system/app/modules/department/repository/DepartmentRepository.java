package vn.system.app.modules.department.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.dashboard.domain.response.DepartmentCompletenessProjection;

@Repository
public interface DepartmentRepository
        extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"company"})
    @Override
    List<Department> findAll();

    @EntityGraph(attributePaths = {"company"})
    @Override
    Page<Department> findAll(Specification<Department> spec, Pageable pageable);

    // ✅ check trùng code trong cùng công ty
    boolean existsByCodeAndCompany_Id(String code, Long companyId);

    // lấy phòng ban theo công ty
    List<Department> findByCompanyId(Long companyId);

    long countByCompany_IdIn(java.util.Set<Long> companyIds);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"company"})
    List<Department> findByCompany_IdIn(Collection<Long> companyIds);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"company"})
    List<Department> findByIdIn(Collection<Long> ids);

    String COMPLETENESS_SELECT = """
            SELECT d.id AS departmentId,
                   d.name AS departmentName,
                   c.name AS companyName,
                   EXISTS(SELECT 1 FROM job_position_charts jpc
                          WHERE jpc.department_id = d.id) AS orgChart,
                   EXISTS(SELECT 1 FROM department_objectives obj
                          WHERE obj.department_id = d.id) AS objectives,
                   EXISTS(SELECT 1 FROM department_procedure_mapping dpm
                          WHERE dpm.department_id = d.id) AS departmentProcedure,
                   EXISTS(SELECT 1 FROM permission_categories pc
                          WHERE pc.department_id = d.id AND pc.active = TRUE) AS permissions,
                   EXISTS(SELECT 1 FROM career_paths cp
                          WHERE cp.department_id = d.id AND cp.active = TRUE) AS careerPath,
                   EXISTS(SELECT 1 FROM salary_grades sg
                          WHERE sg.context_type = 'DEPARTMENT'
                            AND sg.context_id = d.id
                            AND sg.active = TRUE) AS salaryGrade,
                   EXISTS(SELECT 1 FROM department_job_titles djt
                          WHERE djt.department_id = d.id AND djt.active = TRUE) AS jobTitleMap
            FROM departments d
            JOIN companies c ON c.id = d.company_id
            """;

    @org.springframework.data.jpa.repository.Query(value = COMPLETENESS_SELECT, nativeQuery = true)
    List<DepartmentCompletenessProjection> findCompletenessOverviewAll();

    @org.springframework.data.jpa.repository.Query(
            value = COMPLETENESS_SELECT + " WHERE d.id IN :ids", nativeQuery = true)
    List<DepartmentCompletenessProjection> findCompletenessOverviewByIdIn(
            @Param("ids") Collection<Long> ids);

    @org.springframework.data.jpa.repository.Query(
            value = COMPLETENESS_SELECT + " WHERE d.company_id IN :companyIds", nativeQuery = true)
    List<DepartmentCompletenessProjection> findCompletenessOverviewByCompanyIdIn(
            @Param("companyIds") Collection<Long> companyIds);
}
