package vn.system.app.modules.sectionsalarygrade.controller;

import java.util.List;
import jakarta.validation.Valid;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.sectionsalarygrade.domain.request.*;
import vn.system.app.modules.sectionsalarygrade.domain.response.*;
import vn.system.app.modules.sectionsalarygrade.service.SectionSalaryGradeService;

@RestController
@RequestMapping("/api/v1/section-salary-grades")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SectionSalaryGradeController {

    private final SectionSalaryGradeService service;

    // CREATE
    @PostMapping
    @ApiMessage("Tạo bậc lương cho section")
    public ResponseEntity<ResSectionSalaryGradeDTO> create(
            @Valid @RequestBody ReqCreateSectionSalaryGradeDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    // UPDATE
    @PutMapping("/{id}")
    @ApiMessage("Cập nhật bậc lương section")
    public ResponseEntity<ResSectionSalaryGradeDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ReqUpdateSectionSalaryGradeDTO req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    // DELETE (SOFT)
    @DeleteMapping("/{id}")
    @ApiMessage("Xoá bậc lương (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // RESTORE
    @PutMapping("/{id}/restore")
    @ApiMessage("Khôi phục bậc lương section")
    public ResponseEntity<ResSectionSalaryGradeDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(service.restore(id));
    }

    // FETCH ALL theo sectionJobTitleId (admin)
    @GetMapping
    @ApiMessage("Danh sách bậc lương theo sectionJobTitleId")
    public ResponseEntity<List<ResSectionSalaryGradeDTO>> fetch(
            @RequestParam Long sectionJobTitleId) {
        return ResponseEntity.ok(service.fetchBySectionJobTitle(sectionJobTitleId));
    }

    // FETCH CÁ NHÂN
    @GetMapping("/my")
    @ApiMessage("Khung lương của tôi (cấp bộ phận)")
    public ResponseEntity<List<ResSectionSalaryGradeDTO>> fetchMy() {
        return ResponseEntity.ok(service.fetchMy());
    }

    // FETCH THEO BỘ PHẬN
    @GetMapping("/my-section")
    @ApiMessage("Khung lương toàn bộ phận của tôi")
    public ResponseEntity<List<ResSectionSalaryGradeDTO>> fetchByMySection() {
        return ResponseEntity.ok(service.fetchByMySection());
    }
}