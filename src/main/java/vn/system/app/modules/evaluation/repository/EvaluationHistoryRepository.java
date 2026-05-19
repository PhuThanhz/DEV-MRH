package vn.system.app.modules.evaluation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.evaluation.domain.EvaluationHistory;

@Repository
public interface EvaluationHistoryRepository extends JpaRepository<EvaluationHistory, Long> {

    List<EvaluationHistory> findByEvaluationRecordIdOrderByPerformedAtDesc(Long evaluationRecordId);

    /** Đếm số lần bị trả lại — dựa trên toStatus = REVISION_NEEDED */
    long countByEvaluationRecordIdAndToStatus(Long evaluationRecordId,
            vn.system.app.modules.evaluation.domain.enums.RecordStatus toStatus);
}
