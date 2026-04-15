package vn.system.app.modules.careerpath.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.careerpath.domain.request.CareerPathBulkRequest;
import vn.system.app.modules.careerpath.domain.request.CareerPathRequest;
import vn.system.app.modules.careerpath.domain.response.CareerPathBulkResult;
import vn.system.app.modules.careerpath.domain.response.CareerPathPreviewResponse;
import vn.system.app.modules.careerpath.domain.response.CareerPathResponse;
import vn.system.app.modules.careerpath.domain.response.ResCareerPathBandGroupDTO;
import vn.system.app.modules.careerpath.service.CareerPathService;
import vn.system.app.modules.jobtitle.domain.response.JobTitleByLevelResponse;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CareerPathController {

    private final CareerPathService service;

    // =====================================================
    // CREATE
    // =====================================================
    @PostMapping("/career-paths")
    @ApiMessage("Tạo mới lộ trình thăng tiến")
    public ResponseEntity<CareerPathResponse> create(@Valid @RequestBody CareerPathRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.handleCreate(req));
    }

    // =====================================================
    // UPDATE
    // =====================================================
    @PutMapping("/career-paths/{id}")
    @ApiMessage("Cập nhật lộ trình thăng tiến")
    public ResponseEntity<CareerPathResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CareerPathRequest req) {
        return ResponseEntity.ok(service.handleUpdate(id, req));
    }

    // =====================================================
    // DEACTIVATE
    // =====================================================
    @PatchMapping("/career-paths/{id}/deactivate")
    @ApiMessage("Vô hiệu hóa lộ trình thăng tiến")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        service.handleDeactivate(id);
        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // GET ONE
    // =====================================================
    @GetMapping("/career-paths/{id}")
    @ApiMessage("Chi tiết lộ trình thăng tiến")
    public ResponseEntity<CareerPathResponse> fetchOne(@PathVariable Long id) {
        return ResponseEntity.ok(service.convertToResponse(service.fetchById(id)));
    }

    // =====================================================
    // GET ALL BY DEPARTMENT
    // =====================================================
    @GetMapping("/departments/{departmentId}/career-paths")
    @ApiMessage("Danh sách lộ trình theo phòng ban")
    public ResponseEntity<List<CareerPathResponse>> fetchByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(service.fetchByDepartment(departmentId));
    }

    // =====================================================
    // GET ACTIVE ONLY
    // =====================================================
    @GetMapping("/career-paths/active")
    @ApiMessage("Danh sách lộ trình đang hoạt động")
    public ResponseEntity<List<CareerPathResponse>> fetchAllActive() {
        return ResponseEntity.ok(service.fetchAllActive());
    }

    // =====================================================
    // GET GROUP BY BAND
    // =====================================================
    @GetMapping("/departments/{departmentId}/career-paths/by-band")
    @ApiMessage("Lộ trình thăng tiến theo từng cấp (band riêng)")
    public ResponseEntity<List<ResCareerPathBandGroupDTO>> fetchByBand(@PathVariable Long departmentId) {
        return ResponseEntity.ok(service.fetchByDepartmentGroupedByBand(departmentId));
    }

    // =====================================================
    // GLOBAL SORT
    // =====================================================
    @GetMapping("/departments/{departmentId}/career-paths/global")
    @ApiMessage("Lộ trình thăng tiến liên cấp")
    public ResponseEntity<List<CareerPathResponse>> fetchGlobal(@PathVariable Long departmentId) {
        return ResponseEntity.ok(service.fetchGlobalCareerPath(departmentId));
    }

    // =====================================================
    // BULK CREATE (MỚI)
    // =====================================================
    @PostMapping("/career-paths/bulk")
    @ApiMessage("Tạo hàng loạt lộ trình thăng tiến")
    public ResponseEntity<CareerPathBulkResult> bulkCreate(@Valid @RequestBody CareerPathBulkRequest req) {
        CareerPathBulkResult result = service.handleBulkCreate(req);

        HttpStatus status;
        if (result.getTotalCreated() == 0) {
            status = HttpStatus.CONFLICT; // 409 — toàn bộ bị skip
        } else if (result.getTotalSkipped() > 0) {
            status = HttpStatus.MULTI_STATUS; // 207 — một phần skip
        } else {
            status = HttpStatus.CREATED; // 201 — tất cả tạo thành công
        }

        return ResponseEntity.status(status).body(result);
    }

    // =====================================================
    // PREVIEW BULK (MỚI)
    // =====================================================
    @PostMapping("/career-paths/preview")
    @ApiMessage("Xem trước hàng loạt lộ trình thăng tiến")
    public ResponseEntity<CareerPathPreviewResponse> previewBulk(
            @RequestBody CareerPathBulkRequest req) {

        return ResponseEntity.ok(
                service.previewBulkCreate(
                        req.getDepartmentId(),
                        req.getJobTitleIds()));
    }

    // =====================================================
    // FETCH JOB TITLES BY LEVEL (MỚI)
    // =====================================================
    @GetMapping("/departments/{departmentId}/job-titles/by-level/{levelCode}")
    @ApiMessage("Danh sách chức danh theo level trong phòng ban")
    public ResponseEntity<List<JobTitleByLevelResponse>> fetchJobTitlesByLevel(
            @PathVariable Long departmentId,
            @PathVariable String levelCode) {
        return ResponseEntity.ok(service.fetchJobTitlesByLevel(levelCode, departmentId));
    }

    // =====================================================
    // COPY CONTENT FROM LEVEL (MỚI)
    // =====================================================
    @GetMapping("/departments/{departmentId}/career-paths/copy-from/{levelCode}")
    @ApiMessage("Sao chép nội dung từ level khác")
    public ResponseEntity<CareerPathRequest> copyFromLevel(
            @PathVariable Long departmentId,
            @PathVariable String levelCode) {
        return ResponseEntity.ok(service.copyContentFromLevel(departmentId, levelCode));
    }
}