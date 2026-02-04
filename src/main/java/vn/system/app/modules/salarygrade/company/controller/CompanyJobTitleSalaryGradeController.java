package vn.system.app.modules.salarygrade.company.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.salarygrade.company.domain.request.*;
import vn.system.app.modules.salarygrade.company.domain.response.*;
import vn.system.app.modules.salarygrade.company.service.CompanyJobTitleSalaryGradeService;

@RestController
@RequestMapping("/api/v1/company-job-title-salary-grades")
@CrossOrigin(origins = "*")
public class CompanyJobTitleSalaryGradeController {

    private final CompanyJobTitleSalaryGradeService service;

    public CompanyJobTitleSalaryGradeController(
            CompanyJobTitleSalaryGradeService service) {
        this.service = service;
    }

    /*
     * CREATE
     */
    @PostMapping
    @ApiMessage("Tạo bậc lương cho chức danh công ty")
    public ResponseEntity<ResCompanyJobTitleSalaryGradeDTO> create(
            @Valid @RequestBody ReqCreateCompanyJobTitleSalaryGradeDTO req) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(req));
    }

    /*
     * UPDATE
     */
    @PutMapping("/{id}")
    @ApiMessage("Cập nhật bậc lương")
    public ResponseEntity<ResCompanyJobTitleSalaryGradeDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ReqUpdateCompanyJobTitleSalaryGradeDTO req) {

        return ResponseEntity.ok(service.update(id, req));
    }

    /*
     * DELETE
     */
    @DeleteMapping("/{id}")
    @ApiMessage("Xoá bậc lương (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /*
     * FETCH – MATCH FRONTEND
     */
    @GetMapping
    @ApiMessage("Danh sách bậc lương theo chức danh công ty")
    public ResponseEntity<List<ResCompanyJobTitleSalaryGradeDTO>> fetch(
            @RequestParam Long companyJobTitleId) {

        return ResponseEntity.ok(
                service.fetchByCompanyJobTitle(companyJobTitleId));
    }
}
