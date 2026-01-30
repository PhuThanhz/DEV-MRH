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
import vn.system.app.common.util.error.IdInvalidException;
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
            @Valid @RequestBody CreateDepartmentRequest req)
            throws IdInvalidException {

        DepartmentResponse res = departmentService.handleCreateDepartment(req);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(res);
    }

    /* ================= UPDATE ================= */

    @PutMapping("/departments/{id}")
    @ApiMessage("Cập nhật phòng ban")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable("id") Long id,
            @RequestBody UpdateDepartmentRequest req)
            throws IdInvalidException {

        DepartmentResponse res = departmentService.handleUpdateDepartment(id, req);
        if (res == null) {
            throw new IdInvalidException(
                    "Phòng ban với id = " + id + " không tồn tại");
        }

        return ResponseEntity.ok(res);
    }

    /* ================= DELETE (SOFT DELETE) ================= */

    @DeleteMapping("/departments/{id}")
    @ApiMessage("Xoá phòng ban")
    public ResponseEntity<Void> deleteDepartment(
            @PathVariable("id") Long id)
            throws IdInvalidException {

        DepartmentResponse res = departmentService.fetchDepartmentById(id);
        if (res == null) {
            throw new IdInvalidException(
                    "Phòng ban với id = " + id + " không tồn tại");
        }

        departmentService.handleDeleteDepartment(id);
        return ResponseEntity.ok().build();
    }

    /* ================= GET ONE ================= */

    @GetMapping("/departments/{id}")
    @ApiMessage("Chi tiết phòng ban")
    public ResponseEntity<DepartmentResponse> fetchDepartmentById(
            @PathVariable("id") Long id)
            throws IdInvalidException {

        DepartmentResponse res = departmentService.fetchDepartmentById(id);
        if (res == null) {
            throw new IdInvalidException(
                    "Phòng ban với id = " + id + " không tồn tại");
        }

        return ResponseEntity.ok(res);
    }

    /* ================= GET ALL ================= */

    @GetMapping("/departments")
    @ApiMessage("Danh sách phòng ban")
    public ResponseEntity<ResultPaginationDTO> fetchAllDepartments(
            @Filter Specification<Department> spec,
            Pageable pageable) {

        return ResponseEntity.ok(
                departmentService.fetchAllDepartments(spec, pageable));
    }
}
