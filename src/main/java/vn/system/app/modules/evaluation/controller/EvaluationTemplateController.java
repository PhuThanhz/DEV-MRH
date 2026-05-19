package vn.system.app.modules.evaluation.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.turkraft.springfilter.boot.Filter;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.evaluation.domain.*;
import vn.system.app.modules.evaluation.domain.response.ResTemplateDTO;
import vn.system.app.modules.evaluation.service.EvaluationMapper;
import vn.system.app.modules.evaluation.service.EvaluationTemplateService;

/**
 * Admin API — Quản lý Template đánh giá HQCV.
 */
@RestController
@RequestMapping("/api/v1/evaluation")
public class EvaluationTemplateController {

    private final EvaluationTemplateService templateService;
    private final EvaluationMapper mapper;

    public EvaluationTemplateController(EvaluationTemplateService templateService, EvaluationMapper mapper) {
        this.templateService = templateService;
        this.mapper = mapper;
    }

    // ═══════════════ TEMPLATE ═══════════════

    @PostMapping("/templates")
    @ApiMessage("Tạo template đánh giá")
    public ResponseEntity<ResTemplateDTO> createTemplate(@Valid @RequestBody EvaluationTemplate template) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResTemplateDTO(templateService.createTemplate(template)));
    }

    @PutMapping("/templates/{id}")
    @ApiMessage("Cập nhật template đánh giá")
    public ResponseEntity<ResTemplateDTO> updateTemplate(
            @PathVariable Long id, @Valid @RequestBody EvaluationTemplate template) {
        return ResponseEntity.ok(mapper.toResTemplateDTO(templateService.updateTemplate(id, template)));
    }

    @PatchMapping("/templates/{id}/publish")
    @ApiMessage("Publish template")
    public ResponseEntity<ResTemplateDTO> publishTemplate(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResTemplateDTO(templateService.publishTemplate(id)));
    }

    @PatchMapping("/templates/{id}/archive")
    @ApiMessage("Archive template")
    public ResponseEntity<ResTemplateDTO> archiveTemplate(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResTemplateDTO(templateService.archiveTemplate(id)));
    }

    @GetMapping("/templates/{id}")
    @ApiMessage("Chi tiết template")
    public ResponseEntity<ResTemplateDTO> fetchTemplate(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResTemplateDTO(templateService.fetchTemplateById(id)));
    }

    @GetMapping("/templates")
    @ApiMessage("Danh sách template")
    public ResponseEntity<ResultPaginationDTO> fetchAllTemplates(
            @Filter Specification<EvaluationTemplate> spec, Pageable pageable) {
        ResultPaginationDTO page = templateService.fetchAllTemplates(spec, pageable);
        @SuppressWarnings("unchecked")
        List<EvaluationTemplate> templates = (List<EvaluationTemplate>) page.getResult();
        return ResponseEntity.ok(mapper.mapPagination(
                page,
                templates.stream().map(mapper::toResTemplateDTO).toList()));
    }

    @GetMapping("/templates/active")
    @ApiMessage("Danh sách template đang active")
    public ResponseEntity<List<ResTemplateDTO>> fetchActiveTemplates() {
        return ResponseEntity.ok(templateService.fetchActiveTemplates().stream().map(mapper::toResTemplateDTO).toList());
    }

    // ═══════════════ SECTION ═══════════════

    @PostMapping("/templates/{templateId}/sections")
    @ApiMessage("Tạo section trong template")
    public ResponseEntity<ResTemplateDTO.ResSectionDTO> createSection(
            @PathVariable Long templateId, @RequestBody TemplateSection section) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResSectionDTO(templateService.createSection(templateId, section)));
    }

    @PutMapping("/sections/{sectionId}")
    @ApiMessage("Cập nhật section")
    public ResponseEntity<ResTemplateDTO.ResSectionDTO> updateSection(
            @PathVariable Long sectionId, @RequestBody TemplateSection section) {
        return ResponseEntity.ok(mapper.toResSectionDTO(templateService.updateSection(sectionId, section)));
    }

    @DeleteMapping("/sections/{sectionId}")
    @ApiMessage("Xóa section")
    public ResponseEntity<Void> deleteSection(@PathVariable Long sectionId) {
        templateService.deleteSection(sectionId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/templates/{templateId}/sections")
    @ApiMessage("Danh sách section của template")
    public ResponseEntity<List<ResTemplateDTO.ResSectionDTO>> fetchSections(@PathVariable Long templateId) {
        return ResponseEntity.ok(templateService.fetchSectionsByTemplate(templateId).stream().map(mapper::toResSectionDTO).toList());
    }

    // ═══════════════ CRITERIA ═══════════════

    @PostMapping("/sections/{sectionId}/criteria")
    @ApiMessage("Tạo tiêu chí trong section")
    public ResponseEntity<ResTemplateDTO.ResCriteriaDTO> createCriteria(
            @PathVariable Long sectionId, @RequestBody TemplateCriteria criteria) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResCriteriaDTO(templateService.createCriteria(sectionId, criteria)));
    }

    @PutMapping("/criteria/{criteriaId}")
    @ApiMessage("Cập nhật tiêu chí")
    public ResponseEntity<ResTemplateDTO.ResCriteriaDTO> updateCriteria(
            @PathVariable Long criteriaId, @RequestBody TemplateCriteria criteria) {
        return ResponseEntity.ok(mapper.toResCriteriaDTO(templateService.updateCriteria(criteriaId, criteria)));
    }

    @DeleteMapping("/criteria/{criteriaId}")
    @ApiMessage("Xóa tiêu chí")
    public ResponseEntity<Void> deleteCriteria(@PathVariable Long criteriaId) {
        templateService.deleteCriteria(criteriaId);
        return ResponseEntity.ok().build();
    }

    // ═══════════════ CRITERIA LEVELS ═══════════════

    @PostMapping("/criteria/{criteriaId}/levels")
    @ApiMessage("Tạo mô tả mức điểm cho tiêu chí")
    public ResponseEntity<ResTemplateDTO.ResCriteriaLevelDTO> createLevel(
            @PathVariable Long criteriaId, @RequestBody TemplateCriteriaLevel level) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResCriteriaLevelDTO(templateService.createLevel(criteriaId, level)));
    }

    @PutMapping("/levels/{levelId}")
    @ApiMessage("Cập nhật mô tả mức điểm")
    public ResponseEntity<ResTemplateDTO.ResCriteriaLevelDTO> updateLevel(
            @PathVariable Long levelId, @RequestBody TemplateCriteriaLevel level) {
        return ResponseEntity.ok(mapper.toResCriteriaLevelDTO(templateService.updateLevel(levelId, level)));
    }

    @GetMapping("/criteria/{criteriaId}/levels")
    @ApiMessage("Danh sách mức điểm của tiêu chí")
    public ResponseEntity<List<ResTemplateDTO.ResCriteriaLevelDTO>> fetchLevels(@PathVariable Long criteriaId) {
        return ResponseEntity.ok(templateService.fetchLevelsByCriteria(criteriaId).stream().map(mapper::toResCriteriaLevelDTO).toList());
    }
}
