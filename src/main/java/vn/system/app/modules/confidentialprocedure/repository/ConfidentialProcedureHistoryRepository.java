package vn.system.app.modules.confidentialprocedure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.confidentialprocedure.domain.ConfidentialProcedureHistory;

@Repository
public interface ConfidentialProcedureHistoryRepository
        extends JpaRepository<ConfidentialProcedureHistory, Long> {

    List<ConfidentialProcedureHistory> findByProcedure_IdOrderByVersionDesc(Long procedureId);
}