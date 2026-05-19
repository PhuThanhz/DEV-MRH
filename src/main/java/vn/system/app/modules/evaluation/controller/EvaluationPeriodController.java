package vn.system.app.modules.evaluation.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.evaluation.domain.*;
import vn.system.app.modules.evaluation.domain.response.*;
import vn.system.app.modules.evaluation.domain.enums.TemplateType;
import vn.system.app.modules.evaluation.service.EvaluationMapper;
import vn.system.app.modules.evaluation.service.EvaluationPeriodService;

/**
 * Admin API — Quản lý Kỳ đánh giá HQCV.
 */
@RestController
@RequestMapping("/api/v1/evaluation")
public class EvaluationPeriodController {

    private final EvaluationPeriodService periodService;
    private final EvaluationMapper mapper;

    public EvaluationPeriodController(EvaluationPeriodService periodService, EvaluationMapper mapper) {
        this.periodService = periodService;
        this.mapper = mapper;
    }

    // ═══════════════ PERIOD CRUD ═══════════════

    @PostMapping("/periods")
    @ApiMessage("Tạo kỳ đánh giá")
    public ResponseEntity<ResPeriodDTO> createPeriod(@RequestBody EvaluationPeriod period) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResPeriodDTO(periodService.createPeriod(period)));
    }

    @PutMapping("/periods/{id}")
    @ApiMessage("Cập nhật kỳ đánh giá")
    public ResponseEntity<ResPeriodDTO> updatePeriod(
            @PathVariable Long id, @RequestBody EvaluationPeriod period) {
        return ResponseEntity.ok(mapper.toResPeriodDTO(periodService.updatePeriod(id, period)));
    }

    @GetMapping("/periods/{id}")
    @ApiMessage("Chi tiết kỳ đánh giá")
    public ResponseEntity<ResPeriodDTO> fetchPeriod(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResPeriodDTO(periodService.fetchPeriodById(id)));
    }

    @GetMapping("/periods")
    @ApiMessage("Danh sách kỳ đánh giá")
    public ResponseEntity<ResultPaginationDTO> fetchAllPeriods(
            @Filter Specification<EvaluationPeriod> spec, Pageable pageable) {
        ResultPaginationDTO page = periodService.fetchAllPeriods(spec, pageable);
        @SuppressWarnings("unchecked")
        List<EvaluationPeriod> periods = (List<EvaluationPeriod>) page.getResult();
        return ResponseEntity.ok(mapper.mapPagination(
                page,
                periods.stream().map(mapper::toResPeriodDTO).toList()));
    }

    // ═══════════════ PERIOD TEMPLATES ═══════════════

    @PostMapping("/periods/{periodId}/templates")
    @ApiMessage("Gắn template vào kỳ đánh giá")
    public ResponseEntity<ResPeriodTemplateDTO> addTemplateToPeriod(
            @PathVariable Long periodId,
            @RequestBody Map<String, Object> body) {
        Long templateId = Long.valueOf(body.get("templateId").toString());
        TemplateType applyToRole = TemplateType.valueOf(body.get("applyToRole").toString());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResPeriodTemplateDTO(periodService.addTemplateToPeriod(periodId, templateId, applyToRole)));
    }

    @GetMapping("/periods/{periodId}/templates")
    @ApiMessage("Danh sách template trong kỳ")
    public ResponseEntity<List<ResPeriodTemplateDTO>> fetchTemplates(@PathVariable Long periodId) {
        return ResponseEntity.ok(periodService.fetchTemplatesByPeriod(periodId).stream().map(mapper::toResPeriodTemplateDTO).toList());
    }

    // ═══════════════ PERIOD EMPLOYEES ═══════════════

    @PostMapping("/periods/{periodId}/employees")
    @ApiMessage("Thêm nhân viên vào kỳ đánh giá")
    public ResponseEntity<ResPeriodEmployeeDTO> addEmployee(
            @PathVariable Long periodId,
            @RequestBody vn.system.app.modules.evaluation.domain.request.AddPeriodEmployeeRequest req) {
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResPeriodEmployeeDTO(periodService.addEmployeeToPeriod(periodId, req.getEmployeeId(), req.getDirectManagerId(), req.getTemplateId())));
    }

    @PatchMapping("/period-employees/{id}/cancel")
    @ApiMessage("Hủy bản đánh giá nhân viên nghỉ việc")
    public ResponseEntity<ResPeriodEmployeeDTO> cancelEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResPeriodEmployeeDTO(periodService.cancelEmployee(id)));
    }

    @GetMapping("/periods/{periodId}/employees")
    @ApiMessage("Danh sách nhân viên trong kỳ")
    public ResponseEntity<List<ResPeriodEmployeeDTO>> fetchEmployees(@PathVariable Long periodId) {
        return ResponseEntity.ok(periodService.fetchEmployeesByPeriod(periodId).stream().map(mapper::toResPeriodEmployeeDTO).toList());
    }

    // ═══════════════ KÍCH HOẠT / ĐÓNG KỲ ═══════════════

    @PatchMapping("/periods/{id}/activate")
    @ApiMessage("Kích hoạt kỳ đánh giá")
    public ResponseEntity<ResPeriodDTO> activatePeriod(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResPeriodDTO(periodService.activatePeriod(id)));
    }

    @PatchMapping("/periods/{id}/close")
    @ApiMessage("Đóng kỳ đánh giá")
    public ResponseEntity<ResPeriodDTO> closePeriod(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResPeriodDTO(periodService.closePeriod(id)));
    }
}
