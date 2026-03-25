package vn.system.app.modules.companyprocedure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.companyprocedure.domain.CompanyProcedure;

@Repository
public interface CompanyProcedureRepository extends
        JpaRepository<CompanyProcedure, Long>,
        JpaSpecificationExecutor<CompanyProcedure> {

    List<CompanyProcedure> findByDepartment_Id(Long departmentId);

    List<CompanyProcedure> findBySection_Id(Long sectionId);

    boolean existsByDepartment_IdAndProcedureName(Long departmentId, String procedureName);

    // ← THÊM: filter theo công ty
    List<CompanyProcedure> findByDepartment_Company_Id(Long companyId);
}