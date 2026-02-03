package vn.system.app.modules.careerpath.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.careerpath.domain.request.CareerPathRequest;
import vn.system.app.modules.careerpath.domain.response.CareerPathResponse;
import vn.system.app.modules.careerpath.domain.response.ResCareerPathBandGroupDTO;
import vn.system.app.modules.careerpath.service.CareerPathService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CareerPathController {

    private final CareerPathService service;

    /*
     * =====================================================
     * CREATE
     * =====================================================
     */
    @PostMapping("/career-paths")
    @ApiMessage("Tạo mới lộ trình thăng tiến")
    public ResponseEntity<CareerPathResponse> create(@Valid @RequestBody CareerPathRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.handleCreate(req));
    }

    /*
     * =====================================================
     * UPDATE
     * =====================================================
     */
    @PutMapping("/career-paths/{id}")
    @ApiMessage("Cập nhật lộ trình thăng tiến")
    public ResponseEntity<CareerPathResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CareerPathRequest req) {

        return ResponseEntity.ok(service.handleUpdate(id, req));
    }

    /*
     * =====================================================
     * DEACTIVATE (SOFT DELETE)
     * =====================================================
     */
    @PatchMapping("/career-paths/{id}/deactivate")
    @ApiMessage("Vô hiệu hóa lộ trình thăng tiến")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        service.handleDeactivate(id);
        return ResponseEntity.noContent().build();
    }

    /*
     * =====================================================
     * GET ONE
     * =====================================================
     */
    @GetMapping("/career-paths/{id}")
    @ApiMessage("Chi tiết lộ trình thăng tiến")
    public ResponseEntity<CareerPathResponse> fetchOne(@PathVariable Long id) {
        return ResponseEntity.ok(service.convertToResponse(service.fetchById(id)));
    }

    /*
     * =====================================================
     * GET ALL BY DEPARTMENT
     * =====================================================
     */
    @GetMapping("/departments/{departmentId}/career-paths")
    @ApiMessage("Danh sách lộ trình theo phòng ban")
    public ResponseEntity<List<CareerPathResponse>> fetchByDepartment(
            @PathVariable Long departmentId) {

        return ResponseEntity.ok(service.fetchByDepartment(departmentId));
    }

    /*
     * =====================================================
     * GET ACTIVE ONLY
     * =====================================================
     */
    @GetMapping("/career-paths/active")
    @ApiMessage("Danh sách lộ trình đang hoạt động")
    public ResponseEntity<List<CareerPathResponse>> fetchAllActive() {
        return ResponseEntity.ok(service.fetchAllActive());
    }

    /*
     * =====================================================
     * GET GROUP BY BAND
     * =====================================================
     */
    @GetMapping("/departments/{departmentId}/career-paths/by-band")
    @ApiMessage("Lộ trình thăng tiến theo từng cấp (band riêng)")
    public ResponseEntity<List<ResCareerPathBandGroupDTO>> fetchByBand(
            @PathVariable Long departmentId) {

        return ResponseEntity.ok(service.fetchByDepartmentGroupedByBand(departmentId));
    }

    /*
     * =====================================================
     * GLOBAL (SORT CROSS-LEVEL)
     * =====================================================
     */
    @GetMapping("/departments/{departmentId}/career-paths/global")
    @ApiMessage("Lộ trình thăng tiến liên cấp")
    public ResponseEntity<List<CareerPathResponse>> fetchGlobal(
            @PathVariable Long departmentId) {

        return ResponseEntity.ok(service.fetchGlobalCareerPath(departmentId));
    }
}
