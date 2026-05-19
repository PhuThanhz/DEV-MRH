package vn.system.app.modules.evaluation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.evaluation.domain.EvaluationScore;
import vn.system.app.modules.evaluation.domain.enums.ScoredBy;

@Repository
public interface EvaluationScoreRepository extends JpaRepository<EvaluationScore, Long> {

    List<EvaluationScore> findByEvaluationRecordId(Long evaluationRecordId);

    List<EvaluationScore> findByEvaluationRecordIdAndScoredBy(Long evaluationRecordId, ScoredBy scoredBy);

    Optional<EvaluationScore> findByEvaluationRecordIdAndCriteriaIdAndScoredBy(
            Long evaluationRecordId, Long criteriaId, ScoredBy scoredBy);

    void deleteByEvaluationRecordIdAndScoredBy(Long evaluationRecordId, ScoredBy scoredBy);
}
