package vn.system.app.modules.departmentsalarygrade.controller;

import java.util.List;
import jakarta.validation.Valid;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.departmentsalarygrade.domain.request.*;
import vn.system.app.modules.departmentsalarygrade.domain.response.*;
import vn.system.app.modules.departmentsalarygrade.service.DepartmentSalaryGradeService;

@RestController
@RequestMapping("/api/v1/department-salary-grades")
@CrossOrigin(origins = "*")
public class DepartmentSalaryGradeController {

    private final DepartmentSalaryGradeService service;

    public DepartmentSalaryGradeController(DepartmentSalaryGradeService service) {
        this.service = service;
    }

    /* CREATE */
    @PostMapping
    @ApiMessage("Tạo bậc lương phòng ban")
    public ResponseEntity<ResDepartmentSalaryGradeDTO> create(
            @Valid @RequestBody ReqCreateDepartmentSalaryGradeDTO req) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(req));
    }

    /* UPDATE */
    @PutMapping("/{id}")
    @ApiMessage("Cập nhật bậc lương phòng ban")
    public ResponseEntity<ResDepartmentSalaryGradeDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ReqUpdateDepartmentSalaryGradeDTO req) {

        return ResponseEntity.ok(service.update(id, req));
    }

    /* DELETE (SOFT) */
    @DeleteMapping("/{id}")
    @ApiMessage("Xoá bậc lương (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /* RESTORE */
    @PutMapping("/{id}/restore")
    @ApiMessage("Khôi phục bậc lương phòng ban")
    public ResponseEntity<ResDepartmentSalaryGradeDTO> restore(@PathVariable Long id) {
        return ResponseEntity.ok(service.restore(id));
    }

    /* FETCH */
    @GetMapping
    @ApiMessage("Danh sách bậc lương theo departmentJobTitleId")
    public ResponseEntity<List<ResDepartmentSalaryGradeDTO>> fetch(
            @RequestParam("departmentJobTitleId") Long departmentJobTitleId) {

        return ResponseEntity.ok(
                service.fetchByDepartmentJobTitle(departmentJobTitleId));
    }
}
