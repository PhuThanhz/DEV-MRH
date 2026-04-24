package vn.system.app.modules.confidentialprocedure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.modules.confidentialprocedure.domain.ConfidentialProcedure;

@Repository
public interface ConfidentialProcedureRepository extends
                JpaRepository<ConfidentialProcedure, Long>,
                JpaSpecificationExecutor<ConfidentialProcedure> {

        List<ConfidentialProcedure> findByDepartment_Id(Long departmentId);

        List<ConfidentialProcedure> findBySection_Id(Long sectionId);

        List<ConfidentialProcedure> findByDepartment_Company_Id(Long companyId);

        boolean existsByDepartment_IdAndProcedureName(Long departmentId, String procedureName);

        boolean existsByDepartment_IdAndProcedureCode(Long departmentId, String procedureCode);

        boolean existsByDepartment_IdAndProcedureCodeAndIdNot(Long departmentId, String procedureCode, Long id);

        Optional<ConfidentialProcedure> findByQrToken(String qrToken);

        @Modifying
        @Transactional
        @Query("UPDATE ConfidentialProcedure e SET e.qrToken = :token, e.qrCode = :qrCode WHERE e.id = :id")
        void updateQrTokenAndCode(@Param("id") Long id,
                        @Param("token") String token,
                        @Param("qrCode") String qrCode);

        List<ConfidentialProcedure> findByQrTokenIsNull(); // ← THÊM DÒNG NÀY

}