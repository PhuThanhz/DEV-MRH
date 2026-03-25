package vn.system.app.modules.careerpathtemplate.controller;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.careerpathtemplate.domain.request.CareerPathTemplateRequest;
import vn.system.app.modules.careerpathtemplate.domain.response.CareerPathTemplateResponse;
import vn.system.app.modules.careerpathtemplate.service.CareerPathTemplateService;

@RestController
@RequestMapping("/api/v1/career-path-templates")
@RequiredArgsConstructor
public class CareerPathTemplateController {

    private final CareerPathTemplateService service;

    // =====================================================
    // POST /api/v1/career-path-templates
    // =====================================================
    @PostMapping
    @ApiMessage("Tạo template lộ trình thăng tiến")
    public ResponseEntity<CareerPathTemplateResponse> create(
            @Valid @RequestBody CareerPathTemplateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.handleCreate(req));
    }

    // =====================================================
    // PUT /api/v1/career-path-templates/{id}
    // =====================================================
    @PutMapping("/{id}")
    @ApiMessage("Cập nhật template lộ trình thăng tiến")
    public ResponseEntity<CareerPathTemplateResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CareerPathTemplateRequest req) {
        return ResponseEntity.ok(service.handleUpdate(id, req));
    }

    // =====================================================
    // PATCH /api/v1/career-path-templates/{id}/deactivate
    // =====================================================
    @PatchMapping("/{id}/deactivate")
    @ApiMessage("Vô hiệu hoá template lộ trình")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        service.handleDeactivate(id);
        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // PATCH /api/v1/career-path-templates/{id}/activate
    // =====================================================
    @PatchMapping("/{id}/activate")
    @ApiMessage("Kích hoạt template lộ trình")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        service.handleActivate(id);
        return ResponseEntity.noContent().build();
    }

    // =====================================================
    // GET /api/v1/career-path-templates/{id}
    // =====================================================
    @GetMapping("/{id}")
    @ApiMessage("Chi tiết template lộ trình")
    public ResponseEntity<CareerPathTemplateResponse> fetchOne(@PathVariable Long id) {
        return ResponseEntity.ok(service.fetchOne(id));
    }

    // =====================================================
    // GET /api/v1/career-path-templates
    // Tất cả (kể inactive — cho admin)
    // =====================================================
    @GetMapping
    @ApiMessage("Danh sách tất cả template lộ trình")
    public ResponseEntity<List<CareerPathTemplateResponse>> fetchAll() {
        return ResponseEntity.ok(service.fetchAll());
    }

    // =====================================================
    // GET /api/v1/career-path-templates/active
    // Chỉ active (dùng khi assign nhân viên)
    // =====================================================
    @GetMapping("/active")
    @ApiMessage("Danh sách template lộ trình đang hoạt động")
    public ResponseEntity<List<CareerPathTemplateResponse>> fetchAllActive() {
        return ResponseEntity.ok(service.fetchAllActive());
    }

    // =====================================================
    // GET /api/v1/career-path-templates/by-department/{departmentId}
    // Lấy template active theo phòng ban — dùng khi assign nhân viên
    // =====================================================
    @GetMapping("/by-department/{departmentId}")
    @ApiMessage("Danh sách template lộ trình theo phòng ban")
    public ResponseEntity<List<CareerPathTemplateResponse>> fetchByDepartment(
            @PathVariable Long departmentId) {
        return ResponseEntity.ok(service.fetchByDepartment(departmentId));
    }

    // =====================================================
    // GET /api/v1/career-path-templates/by-department/{departmentId}/all
    // Lấy tất cả template theo phòng ban (kể inactive — cho admin)
    // =====================================================
    @GetMapping("/by-department/{departmentId}/all")
    @ApiMessage("Tất cả template lộ trình theo phòng ban")
    public ResponseEntity<List<CareerPathTemplateResponse>> fetchAllByDepartment(
            @PathVariable Long departmentId) {
        return ResponseEntity.ok(service.fetchAllByDepartment(departmentId));
    }
}