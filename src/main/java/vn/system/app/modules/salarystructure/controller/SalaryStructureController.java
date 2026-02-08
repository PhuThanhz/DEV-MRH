package vn.system.app.modules.salarystructure.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;

import vn.system.app.modules.salarystructure.domain.SalaryStructure;
import vn.system.app.modules.salarystructure.domain.request.ReqUpsertSalaryStructureDTO;
import vn.system.app.modules.salarystructure.service.SalaryStructureService;
import vn.system.app.modules.salarystructure.service.SalaryMatrixService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class SalaryStructureController {

    private final SalaryStructureService service;
    private final SalaryMatrixService matrixService;

    /*
     * ============================================================
     * UPSERT CẤU TRÚC LƯƠNG
     * ============================================================
     */
    @PostMapping("/salary-structures/upsert")
    @ApiMessage("Tạo hoặc cập nhật cấu trúc lương")
    public ResponseEntity<?> upsert(@Valid @RequestBody ReqUpsertSalaryStructureDTO req) {
        return ResponseEntity.ok(service.upsert(req));
    }

    /*
     * ============================================================
     * PAGINATION
     * ============================================================
     */
    @GetMapping("/salary-structures")
    @ApiMessage("Danh sách cấu trúc lương (pagination)")
    public ResponseEntity<ResultPaginationDTO> list(
            @Filter Specification<SalaryStructure> spec,
            Pageable pageable) {

        return ResponseEntity.ok(service.fetchAll(spec, pageable));
    }

    /*
     * ============================================================
     * CHI TIẾT 1 CẤU TRÚC LƯƠNG
     * ============================================================
     */
    @GetMapping("/salary-structures/{id}")
    @ApiMessage("Chi tiết cấu trúc lương")
    public ResponseEntity<?> detail(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /*
     * ============================================================
     * MATRIX API (THEO DẠNG FE GỌI)
     * GET /api/v1/departments/{id}/salary-matrix
     * ============================================================
     */
    @GetMapping("/departments/{departmentId}/salary-matrix")
    @ApiMessage("Bảng khung lương theo phòng ban")
    public ResponseEntity<?> getDepartmentMatrix(@PathVariable Long departmentId) {
        return ResponseEntity.ok(matrixService.getDepartmentSalaryMatrix(departmentId));
    }

    /*
     * ============================================================
     * MATRIX API CŨ (VẪN GIỮ LẠI CHO HỆ THỐNG KHÁC NẾU CẦN)
     * GET /api/v1/salary-structures/matrix?departmentId=1
     * ============================================================
     */
    @GetMapping("/salary-structures/matrix")
    @ApiMessage("Bảng cấu trúc lương theo chức danh + bậc lương")
    public ResponseEntity<?> getSalaryMatrixOld(
            @RequestParam("departmentId") Long departmentId) {

        return ResponseEntity.ok(matrixService.getDepartmentSalaryMatrix(departmentId));
    }
}
