package vn.system.app.modules.companyprocedure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.companyprocedure.domain.CompanyProcedureHistory;

@Repository
public interface CompanyProcedureHistoryRepository
        extends JpaRepository<CompanyProcedureHistory, Long> {

    List<CompanyProcedureHistory> findByProcedure_IdOrderByVersionDesc(Long procedureId);
}