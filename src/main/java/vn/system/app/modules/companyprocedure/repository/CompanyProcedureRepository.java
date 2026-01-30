package vn.system.app.modules.companyprocedure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.companyprocedure.domain.CompanyProcedure;

@Repository
public interface CompanyProcedureRepository
        extends JpaRepository<CompanyProcedure, Long> {

    // check trùng khi tạo
    boolean existsBySection_IdAndProcedureName(Long sectionId, String procedureName);

    // ===== GET BY DEPARTMENT =====
    List<CompanyProcedure> findBySection_Department_Id(Long departmentId);

    // ===== GET BY SECTION =====
    List<CompanyProcedure> findBySection_Id(Long sectionId);

    // ===== GET ALL COMPANY =====
    List<CompanyProcedure> findAllByOrderBySection_Department_NameAsc();
}
