package vn.system.app.modules.evaluation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.system.app.modules.evaluation.domain.TemplateSection;

@Repository
public interface TemplateSectionRepository extends JpaRepository<TemplateSection, Long> {

    List<TemplateSection> findByTemplateIdOrderByDisplayOrderAsc(Long templateId);
}
