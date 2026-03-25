package vn.system.app.modules.confidentialprocedure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.confidentialprocedure.domain.ConfidentialProcedure;

@Repository
public interface ConfidentialProcedureRepository extends
        JpaRepository<ConfidentialProcedure, Long>,
        JpaSpecificationExecutor<ConfidentialProcedure> {

    List<ConfidentialProcedure> findByDepartment_Id(Long departmentId);

    List<ConfidentialProcedure> findBySection_Id(Long sectionId);

    List<ConfidentialProcedure> findByDepartment_Company_Id(Long companyId);

    boolean existsByDepartment_IdAndProcedureName(Long departmentId, String procedureName);
}