package vn.system.app.modules.evaluation.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.evaluation.domain.EvaluationScoreAudit;

@Repository
public interface EvaluationScoreAuditRepository extends JpaRepository<EvaluationScoreAudit, Long> {
    List<EvaluationScoreAudit> findByEvaluationRecordIdOrderByChangedAtDesc(Long recordId);
}
