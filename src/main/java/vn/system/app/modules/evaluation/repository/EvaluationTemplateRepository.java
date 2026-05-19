package vn.system.app.modules.evaluation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.evaluation.domain.EvaluationTemplate;
import vn.system.app.modules.evaluation.domain.enums.TemplateStatus;

@Repository
public interface EvaluationTemplateRepository extends JpaRepository<EvaluationTemplate, Long>, JpaSpecificationExecutor<EvaluationTemplate> {

    List<EvaluationTemplate> findByStatus(TemplateStatus status);

    boolean existsByNameAndIdNot(String name, Long id);
}
