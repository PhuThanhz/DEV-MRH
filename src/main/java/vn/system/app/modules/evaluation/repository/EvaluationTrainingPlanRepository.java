package vn.system.app.modules.evaluation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.evaluation.domain.EvaluationTrainingPlan;

@Repository
public interface EvaluationTrainingPlanRepository extends JpaRepository<EvaluationTrainingPlan, Long> {

    List<EvaluationTrainingPlan> findByEvaluationRecordId(Long evaluationRecordId);

    void deleteByEvaluationRecordId(Long evaluationRecordId);
}
