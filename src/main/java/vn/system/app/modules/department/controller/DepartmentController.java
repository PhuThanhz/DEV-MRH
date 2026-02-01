package vn.system.app.modules.department.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.department.domain.request.CreateDepartmentRequest;
import vn.system.app.modules.department.domain.request.UpdateDepartmentRequest;
import vn.system.app.modules.department.domain.response.DepartmentResponse;
import vn.system.app.modules.department.service.DepartmentService;

@RestController
@RequestMapping("/api/v1")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /* ================= CREATE ================= */
    @PostMapping("/departments")
    @ApiMessage("Tạo phòng ban mới")
    public ResponseEntity<DepartmentResponse> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest req) {

        DepartmentResponse res = departmentService.handleCreateDepartment(req);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(res);
    }

    /* ================= UPDATE ================= */
    @PutMapping("/departments/{id}")
    @ApiMessage("Cập nhật phòng ban")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @RequestBody UpdateDepartmentRequest req) {

        DepartmentResponse res = departmentService.handleUpdateDepartment(id, req);

        return ResponseEntity.ok(res);
    }

    /* ================= DELETE ================= */
    @DeleteMapping("/departments/{id}")
    @ApiMessage("Xoá phòng ban")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {

        departmentService.handleDeleteDepartment(id);

        return ResponseEntity.ok().build();
    }

    /* ================= GET ONE ================= */
    @GetMapping("/departments/{id}")
    @ApiMessage("Chi tiết phòng ban")
    public ResponseEntity<DepartmentResponse> fetchDepartmentById(@PathVariable Long id) {

        DepartmentResponse res = departmentService.fetchDepartmentById(id);

        return ResponseEntity.ok(res);
    }

    /* ================= GET LIST ================= */
    @GetMapping("/departments")
    @ApiMessage("Danh sách phòng ban")
    public ResponseEntity<ResultPaginationDTO> fetchAllDepartments(
            @Filter Specification<Department> spec,
            Pageable pageable) {

        return ResponseEntity.ok(
                departmentService.fetchAllDepartments(spec, pageable));
    }

    /* ================= ACTIVE ================= */
    @PatchMapping("/departments/{id}/active")
    @ApiMessage("Kích hoạt lại phòng ban")
    public ResponseEntity<DepartmentResponse> activeDepartment(@PathVariable Long id) {
        DepartmentResponse res = departmentService.handleActiveDepartment(id);
        return ResponseEntity.ok(res);
    }

}
