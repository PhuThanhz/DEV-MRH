package vn.system.app.modules.departmentprocedure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.departmentprocedure.domain.DepartmentProcedure;

@Repository
public interface DepartmentProcedureRepository extends
                JpaRepository<DepartmentProcedure, Long>,
                JpaSpecificationExecutor<DepartmentProcedure> {

        // ✅ Đổi từ department_id sang departments (many-to-many)
        @Query("SELECT p FROM DepartmentProcedure p JOIN p.departments d WHERE d.id = :departmentId")
        List<DepartmentProcedure> findByDepartmentId(@Param("departmentId") Long departmentId);

        // ✅ Đổi từ department_id sang departments (many-to-many)
        @Query("SELECT p FROM DepartmentProcedure p JOIN p.departments d WHERE d.company.id = :companyId")
        List<DepartmentProcedure> findByCompanyId(@Param("companyId") Long companyId);

        List<DepartmentProcedure> findBySection_Id(Long sectionId);

        // ✅ Kiểm tra mã quy trình trùng trong phòng ban
        @Query("SELECT COUNT(p) > 0 FROM DepartmentProcedure p JOIN p.departments d WHERE d.id = :departmentId AND p.procedureCode = :code")
        boolean existsByDepartmentIdAndProcedureCode(
                        @Param("departmentId") Long departmentId,
                        @Param("code") String code);

        @Query("SELECT COUNT(p) > 0 FROM DepartmentProcedure p JOIN p.departments d WHERE d.id = :departmentId AND p.procedureCode = :code AND p.id <> :id")
        boolean existsByDepartmentIdAndProcedureCodeAndIdNot(
                        @Param("departmentId") Long departmentId,
                        @Param("code") String code,
                        @Param("id") Long id);
}