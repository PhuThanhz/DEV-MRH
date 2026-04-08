package vn.system.app.modules.departmentprocedure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.departmentprocedure.domain.DepartmentProcedure;

@Repository
public interface DepartmentProcedureRepository extends
                JpaRepository<DepartmentProcedure, Long>,
                JpaSpecificationExecutor<DepartmentProcedure> {

        List<DepartmentProcedure> findByDepartment_Id(Long departmentId);

        List<DepartmentProcedure> findBySection_Id(Long sectionId);

        // ← THÊM: filter theo công ty
        List<DepartmentProcedure> findByDepartment_Company_Id(Long companyId);

        // ==================== PROCEDURE CODE ====================
        boolean existsByDepartment_IdAndProcedureCode(Long departmentId, String procedureCode);

        boolean existsByDepartment_IdAndProcedureCodeAndIdNot(
                        Long departmentId,
                        String procedureCode,
                        Long id);
        // ========================================================
}