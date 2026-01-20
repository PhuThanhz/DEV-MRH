package vn.system.app.modules.department.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.department.domain.request.CreateDepartmentRequest;
import vn.system.app.modules.department.domain.request.UpdateDepartmentRequest;
import vn.system.app.modules.department.domain.response.DepartmentResponse;
import vn.system.app.modules.department.service.DepartmentService;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    // ============================================================
    // CREATE
    // ============================================================
    @PostMapping
    @ApiMessage("Tạo phòng ban mới")
    public ResponseEntity<DepartmentResponse> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest req) {

        DepartmentResponse res = this.departmentService.handleCreateDepartment(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // ============================================================
    // UPDATE
    // ============================================================
    @PutMapping("/{id}")
    @ApiMessage("Cập nhật phòng ban")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable("id") Long id,
            @RequestBody UpdateDepartmentRequest req) {

        DepartmentResponse res = this.departmentService.handleUpdateDepartment(id, req);
        return ResponseEntity.ok(res);
    }

    // ============================================================
    // DELETE (soft delete)
    // ============================================================
    @DeleteMapping("/{id}")
    @ApiMessage("Xoá phòng ban")
    public ResponseEntity<Void> deleteDepartment(@PathVariable("id") Long id) {

        this.departmentService.handleDeleteDepartment(id);
        return ResponseEntity.ok(null);
    }

    // ============================================================
    // GET ONE
    // ============================================================
    @GetMapping("/{id}")
    @ApiMessage("Chi tiết phòng ban")
    public ResponseEntity<DepartmentResponse> fetchDepartmentById(
            @PathVariable("id") Long id) {

        DepartmentResponse res = this.departmentService.fetchDepartmentById(id);
        return ResponseEntity.ok(res);
    }

    // ============================================================
    // GET ALL + FILTER + PAGINATION
    // ============================================================
    @GetMapping
    @ApiMessage("Danh sách phòng ban")
    public ResponseEntity<ResultPaginationDTO> fetchAllDepartments(
            @Filter Specification<?> spec,
            Pageable pageable) {

        ResultPaginationDTO result = this.departmentService.fetchAllDepartments(spec, pageable);
        return ResponseEntity.ok(result);
    }
}
