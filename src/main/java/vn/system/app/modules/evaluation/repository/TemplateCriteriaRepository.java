package vn.system.app.modules.evaluation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.evaluation.domain.TemplateCriteria;

@Repository
public interface TemplateCriteriaRepository extends JpaRepository<TemplateCriteria, Long> {

    List<TemplateCriteria> findBySectionIdOrderByDisplayOrderAsc(Long sectionId);

    List<TemplateCriteria> findByParentCriteriaId(Long parentId);

    List<TemplateCriteria> findByParentCriteriaIdOrderByDisplayOrderAsc(Long parentId);

    /** Tìm tất cả tiêu chí gốc (không có parent) trong một section */
    List<TemplateCriteria> findBySectionIdAndParentCriteriaIsNullOrderByDisplayOrderAsc(Long sectionId);
}
