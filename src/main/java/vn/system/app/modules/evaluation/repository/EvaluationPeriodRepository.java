package vn.system.app.modules.evaluation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.evaluation.domain.EvaluationPeriod;
import vn.system.app.modules.evaluation.domain.enums.PeriodStatus;

@Repository
public interface EvaluationPeriodRepository extends JpaRepository<EvaluationPeriod, Long>, JpaSpecificationExecutor<EvaluationPeriod> {

    List<EvaluationPeriod> findByStatus(PeriodStatus status);
}
