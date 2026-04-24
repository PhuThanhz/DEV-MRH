package vn.system.app.modules.companyprocedure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.modules.companyprocedure.domain.CompanyProcedure;

@Repository
public interface CompanyProcedureRepository extends
                JpaRepository<CompanyProcedure, Long>,
                JpaSpecificationExecutor<CompanyProcedure> {

        List<CompanyProcedure> findByDepartment_Id(Long departmentId);

        List<CompanyProcedure> findBySection_Id(Long sectionId);

        List<CompanyProcedure> findByDepartment_Company_Id(Long companyId);

        boolean existsByDepartment_IdAndProcedureName(Long departmentId, String procedureName);

        boolean existsByDepartment_IdAndProcedureCode(Long departmentId, String procedureCode);

        boolean existsByDepartment_IdAndProcedureCodeAndIdNot(Long departmentId, String procedureCode, Long id);

        Optional<CompanyProcedure> findByQrToken(String qrToken);

        @Modifying
        @Transactional
        @Query("UPDATE CompanyProcedure e SET e.qrToken = :token, e.qrCode = :qrCode WHERE e.id = :id")
        void updateQrTokenAndCode(@Param("id") Long id,
                        @Param("token") String token,
                        @Param("qrCode") String qrCode);

        List<CompanyProcedure> findByQrTokenIsNull(); // ← THÊM DÒNG NÀY
}