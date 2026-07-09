package vn.system.app.modules.departmentprocedure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.modules.departmentprocedure.domain.DepartmentProcedure;

@Repository
public interface DepartmentProcedureRepository extends
                JpaRepository<DepartmentProcedure, Long>,
                JpaSpecificationExecutor<DepartmentProcedure> {

        @Query("SELECT p FROM DepartmentProcedure p JOIN p.departments d WHERE d.id = :departmentId")
        List<DepartmentProcedure> findByDepartmentId(@Param("departmentId") Long departmentId);

        @Query("SELECT p FROM DepartmentProcedure p JOIN p.departments d WHERE d.company.id = :companyId")
        List<DepartmentProcedure> findByCompanyId(@Param("companyId") Long companyId);

        List<DepartmentProcedure> findBySection_Id(Long sectionId);

        @Query("SELECT COUNT(p) > 0 FROM DepartmentProcedure p JOIN p.departments d WHERE d.id = :departmentId AND p.procedureCode = :code")
        boolean existsByDepartmentIdAndProcedureCode(@Param("departmentId") Long departmentId,
                        @Param("code") String code);

        @Query("SELECT COUNT(p) > 0 FROM DepartmentProcedure p JOIN p.departments d WHERE d.id = :departmentId AND p.procedureCode = :code AND p.id <> :id")
        boolean existsByDepartmentIdAndProcedureCodeAndIdNot(@Param("departmentId") Long departmentId,
                        @Param("code") String code,
                        @Param("id") Long id);

        Optional<DepartmentProcedure> findByQrToken(String qrToken);

        @Modifying
        @Transactional
        @Query("UPDATE DepartmentProcedure e SET e.qrToken = :token, e.qrCode = :qrCode WHERE e.id = :id")
        void updateQrTokenAndCode(@Param("id") Long id,
                        @Param("token") String token,
                        @Param("qrCode") String qrCode);

        List<DepartmentProcedure> findByQrTokenIsNull(); // ← THÊM DÒNG NÀY

        @Query("SELECT DISTINCT d.id FROM DepartmentProcedure p JOIN p.departments d WHERE d.id IN :departmentIds")
        java.util.Set<Long> findDepartmentIdsWithProcedure(@Param("departmentIds") java.util.Collection<Long> departmentIds);
}