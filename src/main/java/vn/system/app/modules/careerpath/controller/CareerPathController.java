package vn.system.app.modules.careerpath.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.careerpath.domain.request.CareerPathRequest;
import vn.system.app.modules.careerpath.domain.response.CareerPathResponse;
import vn.system.app.modules.careerpath.service.CareerPathService;

@RestController
@RequestMapping("/api/v1/career-paths")
@RequiredArgsConstructor
public class CareerPathController {

    private final CareerPathService service;

    /*
     * =====================================================
     * CREATE
     * =====================================================
     */
    @PostMapping
    @ApiMessage("Tạo mới lộ trình thăng tiến")
    public ResponseEntity<CareerPathResponse> create(@RequestBody CareerPathRequest request) {
        CareerPathResponse res = service.handleCreate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /*
     * =====================================================
     * UPDATE
     * =====================================================
     */
    @PutMapping("/{id}")
    @ApiMessage("Cập nhật lộ trình thăng tiến")
    public ResponseEntity<CareerPathResponse> update(
            @PathVariable Long id,
            @RequestBody CareerPathRequest request) {
        CareerPathResponse res = service.handleUpdate(id, request);
        return ResponseEntity.ok(res);
    }

    /*
     * =====================================================
     * TOGGLE ACTIVE (BẬT / TẮT)
     * =====================================================
     */
    @PutMapping("/{id}/active")
    @ApiMessage("Thay đổi trạng thái kích hoạt lộ trình thăng tiến")
    public ResponseEntity<Void> toggleActive(@PathVariable Long id) {
        service.handleToggleActive(id);
        return ResponseEntity.ok().build();
    }

    /*
     * =====================================================
     * DELETE
     * =====================================================
     */
    @DeleteMapping("/{id}")
    @ApiMessage("Xóa lộ trình thăng tiến")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.handleDelete(id);
        return ResponseEntity.ok().build();
    }

    /*
     * =====================================================
     * GET ONE
     * =====================================================
     */
    @GetMapping("/{id}")
    @ApiMessage("Chi tiết lộ trình thăng tiến")
    public ResponseEntity<CareerPathResponse> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(service.convertToResponse(service.fetchById(id)));
    }

    /*
     * =====================================================
     * GET BY DEPARTMENT
     * =====================================================
     */
    @GetMapping("/by-department/{departmentId}")
    @ApiMessage("Danh sách lộ trình theo phòng ban")
    public ResponseEntity<List<CareerPathResponse>> getByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(service.fetchByDepartment(departmentId));
    }

    /*
     * =====================================================
     * GET ALL ACTIVE
     * =====================================================
     */
    @GetMapping("/active")
    @ApiMessage("Danh sách lộ trình đang hoạt động")
    public ResponseEntity<List<CareerPathResponse>> getAllActive() {
        return ResponseEntity.ok(service.fetchAllActive());
    }
}
