package vn.system.app.modules.evaluation.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.evaluation.domain.*;
import vn.system.app.modules.evaluation.domain.enums.PeriodStatus;
import vn.system.app.modules.evaluation.domain.enums.TemplateStatus;
import vn.system.app.modules.evaluation.domain.enums.TemplateType;
import vn.system.app.modules.evaluation.repository.*;
import vn.system.app.modules.company.domain.Company;
import vn.system.app.modules.company.repository.CompanyRepository;
import vn.system.app.modules.jobtitle.domain.JobTitle;
import vn.system.app.modules.jobtitle.repository.JobTitleRepository;

/**
 * Service quản lý Template đánh giá HQCV.
 * Giai đoạn 0: Admin chuẩn bị template.
 */
@Service
public class EvaluationTemplateService {

    private final EvaluationTemplateRepository templateRepo;
    private final TemplateSectionRepository sectionRepo;
    private final TemplateCriteriaRepository criteriaRepo;
    private final TemplateCriteriaLevelRepository levelRepo;
    private final PeriodTemplateRepository periodTemplateRepo;
    private final CompanyRepository companyRepo;
    private final JobTitleRepository jobTitleRepo;

    public EvaluationTemplateService(
            EvaluationTemplateRepository templateRepo,
            TemplateSectionRepository sectionRepo,
            TemplateCriteriaRepository criteriaRepo,
            TemplateCriteriaLevelRepository levelRepo,
            PeriodTemplateRepository periodTemplateRepo,
            CompanyRepository companyRepo,
            JobTitleRepository jobTitleRepo) {
        this.templateRepo = templateRepo;
        this.sectionRepo = sectionRepo;
        this.criteriaRepo = criteriaRepo;
        this.levelRepo = levelRepo;
        this.periodTemplateRepo = periodTemplateRepo;
        this.companyRepo = companyRepo;
        this.jobTitleRepo = jobTitleRepo;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEMPLATE CRUD
    // ═══════════════════════════════════════════════════════════════════════════

    @Transactional
    public EvaluationTemplate createTemplate(EvaluationTemplate template) {
        template.setStatus(TemplateStatus.DRAFT);

        // Gắn Company bắt buộc
        if (template.getCompany() == null || template.getCompany().getId() == 0) {
            throw new IdInvalidException("Vui lòng chọn Công ty áp dụng cho mẫu đánh giá");
        }
        Company comp = companyRepo.findById(template.getCompany().getId())
                .orElseThrow(() -> new IdInvalidException("Công ty không tồn tại"));
        template.setCompany(comp);

        // Xử lý targetJobTitles
        if (template.getTargetJobTitles() != null && !template.getTargetJobTitles().isEmpty()) {
            List<Long> titleIds = template.getTargetJobTitles().stream().map(JobTitle::getId).collect(Collectors.toList());
            List<JobTitle> titles = jobTitleRepo.findAllById(titleIds);
            template.setTargetJobTitles(titles);
        } else {
            template.setTargetJobTitles(null);
        }

        return templateRepo.save(template);
    }

    @Transactional
    public EvaluationTemplate updateTemplate(Long id, EvaluationTemplate updates) {
        EvaluationTemplate existing = fetchTemplateById(id);
        checkTemplateEditable(existing);

        if (updates.getName() != null)
            existing.setName(updates.getName());
        if (updates.getType() != null)
            existing.setType(updates.getType());
        if (updates.getDescription() != null)
            existing.setDescription(updates.getDescription());

        // Cập nhật Company bắt buộc (nếu được truyền lên)
        if (updates.getCompany() != null) {
            if (updates.getCompany().getId() == 0) {
                throw new IdInvalidException("Vui lòng chọn Công ty áp dụng cho mẫu đánh giá");
            }
            Company comp = companyRepo.findById(updates.getCompany().getId())
                    .orElseThrow(() -> new IdInvalidException("Công ty không tồn tại"));
            existing.setCompany(comp);
        }

        // Cập nhật targetJobTitles
        if (updates.getTargetJobTitles() != null) {
            if (updates.getTargetJobTitles().isEmpty()) {
                existing.setTargetJobTitles(null);
            } else {
                List<Long> titleIds = updates.getTargetJobTitles().stream().map(JobTitle::getId).collect(Collectors.toList());
                List<JobTitle> titles = jobTitleRepo.findAllById(titleIds);
                existing.setTargetJobTitles(titles);
            }
        }

        return templateRepo.save(existing);
    }

    /**
     * Publish template: DRAFT → ACTIVE.
     * Validate tổng trọng số sections = 1.0 và tổng trọng số criteria trong mỗi
     * section = 1.0.
     */
    @Transactional
    public EvaluationTemplate publishTemplate(Long id) {
        EvaluationTemplate template = fetchTemplateById(id);

        if (template.getStatus() != TemplateStatus.DRAFT) {
            throw new IdInvalidException("Chỉ có thể publish template ở trạng thái DRAFT");
        }

        validateTemplateWeights(template);
        template.setStatus(TemplateStatus.ACTIVE);
        return templateRepo.save(template);
    }

    /** Archive template: ACTIVE → ARCHIVED */
    @Transactional
    public EvaluationTemplate archiveTemplate(Long id) {
        EvaluationTemplate template = fetchTemplateById(id);

        if (template.getStatus() != TemplateStatus.ACTIVE) {
            throw new IdInvalidException("Chỉ có thể archive template ở trạng thái ACTIVE");
        }

        template.setStatus(TemplateStatus.ARCHIVED);
        return templateRepo.save(template);
    }

    public EvaluationTemplate fetchTemplateById(Long id) {
        return templateRepo.findById(id)
                .orElseThrow(() -> new IdInvalidException("Template đánh giá không tồn tại"));
    }

    public List<EvaluationTemplate> fetchActiveTemplates() {
        Specification<EvaluationTemplate> spec = (root, query, cb) -> cb.equal(root.get("status"), TemplateStatus.ACTIVE);
        spec = spec.and(vn.system.app.common.util.ScopeSpec.byCompanyScope("company.id"));
        return templateRepo.findAll(spec);
    }

    public ResultPaginationDTO fetchAllTemplates(Specification<EvaluationTemplate> spec, Pageable pageable) {
        Page<EvaluationTemplate> page = templateRepo.findAll(spec, pageable);

        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        rs.setMeta(meta);
        rs.setResult(page.getContent());
        return rs;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SECTION CRUD
    // ═══════════════════════════════════════════════════════════════════════════

    @Transactional
    public TemplateSection createSection(Long templateId, TemplateSection section) {
        EvaluationTemplate template = fetchTemplateById(templateId);
        checkTemplateEditable(template);

        if (section.getWeight() == null) {
            throw new IdInvalidException("Trọng số của phần không được để trống");
        }
        if (section.getWeight() < 0 || section.getWeight() > 1.0) {
            throw new IdInvalidException("Trọng số của phần phải từ 0% đến 100%");
        }

        // Validate total weight doesn't exceed 100%
        List<TemplateSection> existingSections = sectionRepo.findByTemplateIdOrderByDisplayOrderAsc(templateId);
        double totalWeight = existingSections.stream()
                .mapToDouble(s -> s.getWeight() != null ? s.getWeight() : 0.0)
                .sum();
        totalWeight += section.getWeight();
        if (totalWeight > 1.0001) {
            throw new IdInvalidException("Tổng trọng số của các phần không được vượt quá 100%");
        }

        section.setTemplate(template);
        return sectionRepo.save(section);
    }

    @Transactional
    public TemplateSection updateSection(Long sectionId, TemplateSection updates) {
        TemplateSection existing = sectionRepo.findById(sectionId)
                .orElseThrow(() -> new IdInvalidException("Section không tồn tại"));
        checkTemplateEditable(existing.getTemplate());

        if (updates.getWeight() != null && (updates.getWeight() < 0 || updates.getWeight() > 1.0)) {
            throw new IdInvalidException("Trọng số của phần phải từ 0% đến 100%");
        }

        if (updates.getWeight() != null) {
            // Validate total weight doesn't exceed 100%
            List<TemplateSection> existingSections = sectionRepo
                    .findByTemplateIdOrderByDisplayOrderAsc(existing.getTemplate().getId());
            double totalWeight = 0.0;
            for (TemplateSection s : existingSections) {
                if (!s.getId().equals(sectionId)) {
                    totalWeight += s.getWeight() != null ? s.getWeight() : 0.0;
                }
            }
            totalWeight += updates.getWeight();
            if (totalWeight > 1.0001) {
                throw new IdInvalidException("Tổng trọng số của các phần không được vượt quá 100%");
            }
        }

        if (updates.getCode() != null)
            existing.setCode(updates.getCode());
        if (updates.getName() != null)
            existing.setName(updates.getName());
        if (updates.getWeight() != null)
            existing.setWeight(updates.getWeight());
        if (updates.getDisplayOrder() != null)
            existing.setDisplayOrder(updates.getDisplayOrder());

        return sectionRepo.save(existing);
    }

    @Transactional
    public void deleteSection(Long sectionId) {
        TemplateSection section = sectionRepo.findById(sectionId)
                .orElseThrow(() -> new IdInvalidException("Section không tồn tại"));
        checkTemplateEditable(section.getTemplate());
        sectionRepo.delete(section);
    }

    public List<TemplateSection> fetchSectionsByTemplate(Long templateId) {
        return sectionRepo.findByTemplateIdOrderByDisplayOrderAsc(templateId);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CRITERIA CRUD
    // ═══════════════════════════════════════════════════════════════════════════

    @Transactional
    public TemplateCriteria createCriteria(Long sectionId, TemplateCriteria criteria) {
        TemplateSection section = sectionRepo.findById(sectionId)
                .orElseThrow(() -> new IdInvalidException("Section không tồn tại"));
        checkTemplateEditable(section.getTemplate());

        if (criteria.getWeight() == null) {
            throw new IdInvalidException("Trọng số của tiêu chí không được để trống");
        }
        if (criteria.getWeight() < 0 || criteria.getWeight() > 1.0) {
            throw new IdInvalidException("Trọng số của tiêu chí phải từ 0% đến 100%");
        }

        // Validate total weight of criteria doesn't exceed section weight
        List<TemplateCriteria> existingCriteria = criteriaRepo.findBySectionIdOrderByDisplayOrderAsc(sectionId);
        double totalWeight = existingCriteria.stream()
                .mapToDouble(c -> c.getWeight() != null ? c.getWeight() : 0.0)
                .sum();
        totalWeight += criteria.getWeight();
        double maxWeight = section.getWeight() != null ? section.getWeight() : 0.0;
        if (totalWeight > maxWeight + 0.0001) {
            throw new IdInvalidException("Tổng trọng số của các tiêu chí không được vượt quá trọng số của phần ("
                    + Math.round(maxWeight * 100) + "%)");
        }

        criteria.setSection(section);
        return criteriaRepo.save(criteria);
    }

    @Transactional
    public TemplateCriteria updateCriteria(Long criteriaId, TemplateCriteria updates) {
        TemplateCriteria existing = criteriaRepo.findById(criteriaId)
                .orElseThrow(() -> new IdInvalidException("Tiêu chí không tồn tại"));
        checkTemplateEditable(existing.getSection().getTemplate());

        if (updates.getWeight() != null && (updates.getWeight() < 0 || updates.getWeight() > 1.0)) {
            throw new IdInvalidException("Trọng số của tiêu chí phải từ 0% đến 100%");
        }

        if (updates.getWeight() != null) {
            // Validate total weight of criteria doesn't exceed section weight
            TemplateSection section = existing.getSection();
            List<TemplateCriteria> existingCriteria = criteriaRepo
                    .findBySectionIdOrderByDisplayOrderAsc(section.getId());
            double totalWeight = 0.0;
            for (TemplateCriteria c : existingCriteria) {
                if (!c.getId().equals(criteriaId)) {
                    totalWeight += c.getWeight() != null ? c.getWeight() : 0.0;
                }
            }
            totalWeight += updates.getWeight();
            double maxWeight = section.getWeight() != null ? section.getWeight() : 0.0;
            if (totalWeight > maxWeight + 0.0001) {
                throw new IdInvalidException("Tổng trọng số của các tiêu chí không được vượt quá trọng số của phần ("
                        + Math.round(maxWeight * 100) + "%)");
            }
        }

        if (updates.getName() != null)
            existing.setName(updates.getName());
        if (updates.getMeasurementMethod() != null)
            existing.setMeasurementMethod(updates.getMeasurementMethod());
        if (updates.getWeight() != null)
            existing.setWeight(updates.getWeight());
        if (updates.getDisplayOrder() != null)
            existing.setDisplayOrder(updates.getDisplayOrder());

        return criteriaRepo.save(existing);
    }

    @Transactional
    public void deleteCriteria(Long criteriaId) {
        TemplateCriteria criteria = criteriaRepo.findById(criteriaId)
                .orElseThrow(() -> new IdInvalidException("Tiêu chí không tồn tại"));
        checkTemplateEditable(criteria.getSection().getTemplate());
        criteriaRepo.delete(criteria);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CRITERIA LEVELS CRUD
    // ═══════════════════════════════════════════════════════════════════════════

    @Transactional
    public TemplateCriteriaLevel createLevel(Long criteriaId, TemplateCriteriaLevel level) {
        TemplateCriteria criteria = criteriaRepo.findById(criteriaId)
                .orElseThrow(() -> new IdInvalidException("Tiêu chí không tồn tại"));
        checkTemplateEditable(criteria.getSection().getTemplate());

        if (level.getLevel() < 1 || level.getLevel() > 5) {
            throw new IdInvalidException("Mức điểm phải từ 1 đến 5");
        }

        level.setCriteria(criteria);
        return levelRepo.save(level);
    }

    @Transactional
    public TemplateCriteriaLevel updateLevel(Long levelId, TemplateCriteriaLevel updates) {
        TemplateCriteriaLevel existing = levelRepo.findById(levelId)
                .orElseThrow(() -> new IdInvalidException("Mức điểm không tồn tại"));
        checkTemplateEditable(existing.getCriteria().getSection().getTemplate());

        if (updates.getDescription() != null)
            existing.setDescription(updates.getDescription());

        return levelRepo.save(existing);
    }

    public List<TemplateCriteriaLevel> fetchLevelsByCriteria(Long criteriaId) {
        return levelRepo.findByCriteriaIdOrderByLevelAsc(criteriaId);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // VALIDATION
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Template chỉ editable khi DRAFT.
     * Template đã ACTIVE hoặc ARCHIVED → không cho sửa.
     */
    private void checkTemplateEditable(EvaluationTemplate template) {
        if (template.getStatus() != TemplateStatus.DRAFT) {
            throw new IdInvalidException("Template đã được publish hoặc archive, không thể chỉnh sửa");
        }
    }

    /**
     * Validate:
     * - Tổng weight các section trong template = 1.0
     * - Tổng weight các tiêu chí cha (parent = null) trong mỗi section = 1.0
     */
    private void validateTemplateWeights(EvaluationTemplate template) {
        List<TemplateSection> sections = sectionRepo.findByTemplateIdOrderByDisplayOrderAsc(template.getId());

        if (sections.isEmpty()) {
            throw new IdInvalidException("Template phải có ít nhất một phần (section)");
        }

        double sectionWeightSum = sections.stream()
                .mapToDouble(TemplateSection::getWeight)
                .sum();

        if (Math.abs(sectionWeightSum - 1.0) > 0.001) {
            throw new IdInvalidException(
                    String.format("Tổng trọng số các phần phải bằng 100%%. Hiện tại: %.1f%%", sectionWeightSum * 100));
        }

        for (TemplateSection section : sections) {
            List<TemplateCriteria> rootCriteria = criteriaRepo
                    .findBySectionIdAndParentCriteriaIsNullOrderByDisplayOrderAsc(section.getId());

            if (rootCriteria.isEmpty()) {
                throw new IdInvalidException(
                        String.format("Phần '%s' phải có ít nhất một tiêu chí", section.getName()));
            }

            double criteriaWeightSum = 0;
            for (TemplateCriteria crit : rootCriteria) {
                criteriaWeightSum += crit.getWeight();

                // Validate 5 levels must be fully configured
                List<TemplateCriteriaLevel> levels = levelRepo.findByCriteriaIdOrderByLevelAsc(crit.getId());
                long validLevelCount = levels.stream()
                        .filter(l -> l.getDescription() != null && !l.getDescription().trim().isEmpty())
                        .count();
                if (validLevelCount < 5) {
                    throw new IdInvalidException(
                            String.format(
                                    "Tiêu chí '%s' trong phần '%s' chưa được cấu hình đầy đủ 5 mức điểm. Vui lòng thiết lập mô tả mức điểm trước khi kích hoạt.",
                                    crit.getName(), section.getName()));
                }
            }

            if (Math.abs(criteriaWeightSum - section.getWeight()) > 0.001) {
                throw new IdInvalidException(
                        String.format("Tổng trọng số tiêu chí trong phần '%s' phải bằng %.1f%%. Hiện tại: %.1f%%",
                                section.getName(), section.getWeight() * 100, criteriaWeightSum * 100));
            }
        }
    }
}
