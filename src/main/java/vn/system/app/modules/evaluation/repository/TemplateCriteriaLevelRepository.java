package vn.system.app.modules.evaluation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.evaluation.domain.TemplateCriteriaLevel;

@Repository
public interface TemplateCriteriaLevelRepository extends JpaRepository<TemplateCriteriaLevel, Long> {

    List<TemplateCriteriaLevel> findByCriteriaIdOrderByLevelAsc(Long criteriaId);
}
