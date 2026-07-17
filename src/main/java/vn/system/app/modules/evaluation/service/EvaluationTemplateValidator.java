package vn.system.app.modules.evaluation.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.evaluation.domain.EvaluationTemplate;
import vn.system.app.modules.evaluation.domain.TemplateCriteria;
import vn.system.app.modules.evaluation.domain.TemplateCriteriaLevel;
import vn.system.app.modules.evaluation.domain.TemplateSection;
import vn.system.app.modules.evaluation.repository.TemplateCriteriaLevelRepository;
import vn.system.app.modules.evaluation.repository.TemplateCriteriaRepository;
import vn.system.app.modules.evaluation.repository.TemplateSectionRepository;

/** Validates that a template is complete and safe to use for scoring. */
@Component
public class EvaluationTemplateValidator {

    private static final double WEIGHT_EPSILON = 0.001;
    private static final Set<Integer> REQUIRED_LEVELS = Set.of(1, 2, 3, 4, 5);

    private final TemplateSectionRepository sectionRepo;
    private final TemplateCriteriaRepository criteriaRepo;
    private final TemplateCriteriaLevelRepository levelRepo;

    public EvaluationTemplateValidator(
            TemplateSectionRepository sectionRepo,
            TemplateCriteriaRepository criteriaRepo,
            TemplateCriteriaLevelRepository levelRepo) {
        this.sectionRepo = sectionRepo;
        this.criteriaRepo = criteriaRepo;
        this.levelRepo = levelRepo;
    }

    public void validateReadyForUse(EvaluationTemplate template) {
        if (template == null || template.getId() == null) {
            throw new IdInvalidException("Mẫu đánh giá không hợp lệ");
        }

        List<TemplateSection> sections = sectionRepo
                .findByTemplateIdOrderByDisplayOrderAsc(template.getId());
        if (sections.isEmpty()) {
            throw new IdInvalidException("Mẫu đánh giá phải có ít nhất một phần");
        }

        double sectionWeightSum = 0.0;
        for (TemplateSection section : sections) {
            double sectionWeight = requirePositiveWeight(
                    section.getWeight(), "Trọng số phần '" + section.getName() + "'");
            sectionWeightSum += sectionWeight;
            validateSection(section, sectionWeight);
        }

        if (!approximatelyEqual(sectionWeightSum, 1.0)) {
            throw new IdInvalidException(String.format(
                    "Tổng trọng số các phần phải bằng 100%%. Hiện tại: %.1f%%",
                    sectionWeightSum * 100));
        }
    }

    private void validateSection(TemplateSection section, double sectionWeight) {
        List<TemplateCriteria> rootCriteria = criteriaRepo
                .findBySectionIdAndParentCriteriaIsNullOrderByDisplayOrderAsc(section.getId());
        if (rootCriteria.isEmpty()) {
            throw new IdInvalidException(String.format(
                    "Phần '%s' đang có trọng số %.1f%% nhưng chưa có tiêu chí đánh giá",
                    section.getName(), sectionWeight * 100));
        }

        double criteriaWeightSum = 0.0;
        for (TemplateCriteria criteria : rootCriteria) {
            double criteriaWeight = requirePositiveWeight(
                    criteria.getWeight(), "Trọng số tiêu chí '" + criteria.getName() + "'");
            criteriaWeightSum += criteriaWeight;

            List<TemplateCriteria> children = criteriaRepo
                    .findByParentCriteriaIdOrderByDisplayOrderAsc(criteria.getId());
            if (children.isEmpty()) {
                validateScorableCriteria(criteria, section.getName(), null);
                continue;
            }

            for (TemplateCriteria child : children) {
                if (child.getWeight() == null || !Double.isFinite(child.getWeight())
                        || Math.abs(child.getWeight()) > WEIGHT_EPSILON) {
                    throw new IdInvalidException(String.format(
                            "Tiêu chí con '%s' phải có trọng số 0%% vì dùng chung trọng số của tiêu chí cha '%s'",
                            child.getName(), criteria.getName()));
                }
                if (!criteriaRepo.findByParentCriteriaId(child.getId()).isEmpty()) {
                    throw new IdInvalidException(String.format(
                            "Tiêu chí '%s' có cấu trúc quá 2 cấp. Mẫu đánh giá chỉ hỗ trợ tiêu chí cha và tiêu chí con",
                            criteria.getName()));
                }
                validateScorableCriteria(child, section.getName(), criteria.getName());
            }
        }

        if (!approximatelyEqual(criteriaWeightSum, sectionWeight)) {
            throw new IdInvalidException(String.format(
                    "Tổng trọng số tiêu chí trong phần '%s' phải bằng %.1f%%. Hiện tại: %.1f%%",
                    section.getName(), sectionWeight * 100, criteriaWeightSum * 100));
        }
    }

    private void validateScorableCriteria(
            TemplateCriteria criteria, String sectionName, String parentCriteriaName) {
        List<TemplateCriteriaLevel> levels = levelRepo
                .findByCriteriaIdOrderByLevelAsc(criteria.getId());
        Set<Integer> configuredLevels = new HashSet<>();

        for (TemplateCriteriaLevel level : levels) {
            if (level.getLevel() == null || !REQUIRED_LEVELS.contains(level.getLevel())
                    || level.getDescription() == null || level.getDescription().trim().isEmpty()) {
                throw incompleteLevels(criteria, sectionName, parentCriteriaName);
            }
            configuredLevels.add(level.getLevel());
        }

        if (levels.size() != REQUIRED_LEVELS.size() || !configuredLevels.equals(REQUIRED_LEVELS)) {
            throw incompleteLevels(criteria, sectionName, parentCriteriaName);
        }
    }

    private IdInvalidException incompleteLevels(
            TemplateCriteria criteria, String sectionName, String parentCriteriaName) {
        String criteriaLabel = parentCriteriaName == null
                ? String.format("Tiêu chí '%s'", criteria.getName())
                : String.format("Mục con '%s' của tiêu chí '%s'", criteria.getName(), parentCriteriaName);
        return new IdInvalidException(String.format(
                "%s trong phần '%s' phải có đầy đủ mô tả cho đúng 5 mức điểm từ 1 đến 5",
                criteriaLabel, sectionName));
    }

    private double requirePositiveWeight(Double weight, String label) {
        if (weight == null || !Double.isFinite(weight) || weight <= 0 || weight > 1.0) {
            throw new IdInvalidException(label + " phải lớn hơn 0% và không vượt quá 100%");
        }
        return weight;
    }

    private boolean approximatelyEqual(double left, double right) {
        return Math.abs(left - right) <= WEIGHT_EPSILON;
    }
}
