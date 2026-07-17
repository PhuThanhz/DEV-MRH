package vn.system.app.modules.evaluation.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.evaluation.domain.EvaluationTemplate;
import vn.system.app.modules.evaluation.domain.TemplateCriteria;
import vn.system.app.modules.evaluation.domain.TemplateCriteriaLevel;
import vn.system.app.modules.evaluation.domain.TemplateSection;
import vn.system.app.modules.evaluation.repository.TemplateCriteriaLevelRepository;
import vn.system.app.modules.evaluation.repository.TemplateCriteriaRepository;
import vn.system.app.modules.evaluation.repository.TemplateSectionRepository;

@ExtendWith(MockitoExtension.class)
class EvaluationTemplateValidatorTest {

    @Mock
    private TemplateSectionRepository sectionRepo;
    @Mock
    private TemplateCriteriaRepository criteriaRepo;
    @Mock
    private TemplateCriteriaLevelRepository levelRepo;

    private EvaluationTemplateValidator validator;
    private EvaluationTemplate template;
    private TemplateSection section;

    @BeforeEach
    void setUp() {
        validator = new EvaluationTemplateValidator(sectionRepo, criteriaRepo, levelRepo);

        template = new EvaluationTemplate();
        template.setId(1L);

        section = new TemplateSection();
        section.setId(10L);
        section.setName("Kỹ năng mềm");
        section.setWeight(1.0);
        when(sectionRepo.findByTemplateIdOrderByDisplayOrderAsc(1L)).thenReturn(List.of(section));
    }

    @Test
    void rejectsWeightedSectionWithoutCriteria() {
        when(criteriaRepo.findBySectionIdAndParentCriteriaIsNullOrderByDisplayOrderAsc(10L))
                .thenReturn(List.of());

        IdInvalidException error = assertThrows(
                IdInvalidException.class, () -> validator.validateReadyForUse(template));

        assertTrue(error.getMessage().contains("chưa có tiêu chí"));
    }

    @Test
    void rejectsCriteriaWeightsThatDoNotEqualSectionWeight() {
        TemplateCriteria criteria = rootCriteria(100L, "Giao tiếp", 0.8);
        stubLeafCriteria(criteria);

        IdInvalidException error = assertThrows(
                IdInvalidException.class, () -> validator.validateReadyForUse(template));

        assertTrue(error.getMessage().contains("phải bằng 100.0%"));
    }

    @Test
    void rejectsIncompleteScoreLevels() {
        TemplateCriteria criteria = rootCriteria(100L, "Giao tiếp", 1.0);
        when(criteriaRepo.findBySectionIdAndParentCriteriaIsNullOrderByDisplayOrderAsc(10L))
                .thenReturn(List.of(criteria));
        when(criteriaRepo.findByParentCriteriaIdOrderByDisplayOrderAsc(100L)).thenReturn(List.of());
        when(levelRepo.findByCriteriaIdOrderByLevelAsc(100L)).thenReturn(levels(1, 2, 3, 4));

        IdInvalidException error = assertThrows(
                IdInvalidException.class, () -> validator.validateReadyForUse(template));

        assertTrue(error.getMessage().contains("đúng 5 mức điểm"));
    }

    @Test
    void acceptsAbsoluteCriteriaWeightsAndCompleteLevels() {
        TemplateCriteria criteria = rootCriteria(100L, "Giao tiếp", 1.0);
        stubLeafCriteria(criteria);

        assertDoesNotThrow(() -> validator.validateReadyForUse(template));
    }

    private TemplateCriteria rootCriteria(Long id, String name, double weight) {
        TemplateCriteria criteria = new TemplateCriteria();
        criteria.setId(id);
        criteria.setName(name);
        criteria.setWeight(weight);
        criteria.setSection(section);
        return criteria;
    }

    private void stubLeafCriteria(TemplateCriteria criteria) {
        when(criteriaRepo.findBySectionIdAndParentCriteriaIsNullOrderByDisplayOrderAsc(10L))
                .thenReturn(List.of(criteria));
        when(criteriaRepo.findByParentCriteriaIdOrderByDisplayOrderAsc(criteria.getId()))
                .thenReturn(List.of());
        when(levelRepo.findByCriteriaIdOrderByLevelAsc(criteria.getId()))
                .thenReturn(levels(1, 2, 3, 4, 5));
    }

    private List<TemplateCriteriaLevel> levels(int... values) {
        return java.util.Arrays.stream(values).mapToObj(value -> {
            TemplateCriteriaLevel level = new TemplateCriteriaLevel();
            level.setLevel(value);
            level.setDescription("Mô tả mức " + value);
            return level;
        }).toList();
    }
}
