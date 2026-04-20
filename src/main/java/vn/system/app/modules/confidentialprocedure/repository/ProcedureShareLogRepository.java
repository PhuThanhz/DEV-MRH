package vn.system.app.modules.confidentialprocedure.repository;

import java.util.List;

import org.springframework.data.domain.Page; // ← thêm
import org.springframework.data.domain.Pageable; // ← thêm
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.confidentialprocedure.domain.ProcedureShareLog;

@Repository
public interface ProcedureShareLogRepository
        extends JpaRepository<ProcedureShareLog, Long> {

    List<ProcedureShareLog> findByProcedureIdOrderBySentAtDesc(Long procedureId);

    List<ProcedureShareLog> findBySenderIdOrderBySentAtDesc(String senderId);

    List<ProcedureShareLog> findByReceiverIdOrderBySentAtDesc(String receiverId);

    Page<ProcedureShareLog> findAllByOrderBySentAtDesc(Pageable pageable);
}