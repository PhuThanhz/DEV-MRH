package vn.system.app.modules.evaluation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.evaluation.domain.PeriodTemplate;

@Repository
public interface PeriodTemplateRepository extends JpaRepository<PeriodTemplate, Long> {

    List<PeriodTemplate> findByPeriodId(Long periodId);

    boolean existsByPeriodIdAndTemplateId(Long periodId, Long templateId);

    Optional<PeriodTemplate> findByPeriodIdAndTemplateId(Long periodId, Long templateId);

    boolean existsByTemplateId(Long templateId);
}
