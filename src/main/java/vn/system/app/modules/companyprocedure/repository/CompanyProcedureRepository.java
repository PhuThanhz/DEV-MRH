package vn.system.app.modules.companyprocedure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.system.app.modules.companyprocedure.domain.CompanyProcedure;

@Repository
public interface CompanyProcedureRepository
        extends JpaRepository<CompanyProcedure, Long>, JpaSpecificationExecutor<CompanyProcedure> {

    // =========================
    // EXISTS (check trùng khi tạo)
    // =========================
    boolean existsBySection_IdAndProcedureName(Long sectionId, String procedureName);

    // =========================
    // FETCH BY DEPARTMENT
    // =========================
    List<CompanyProcedure> findBySection_Department_Id(Long departmentId);

    // =========================
    // FETCH BY SECTION
    // =========================
    List<CompanyProcedure> findBySection_Id(Long sectionId);

    // =========================
    // FETCH ALL COMPANY (Toàn bộ công ty)
    // =========================
    List<CompanyProcedure> findAllByOrderBySection_Department_NameAsc();
}
