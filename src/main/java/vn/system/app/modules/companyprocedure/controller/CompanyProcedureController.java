package vn.system.app.modules.companyprocedure.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.companyprocedure.domain.CompanyProcedure;
import vn.system.app.modules.companyprocedure.domain.request.CompanyProcedureRequest;
import vn.system.app.modules.companyprocedure.domain.response.CompanyProcedureResponse;
import vn.system.app.modules.companyprocedure.service.CompanyProcedureService;

@RestController
@RequestMapping("/api/v1/company-procedures")
public class CompanyProcedureController {

    private final CompanyProcedureService service;

    public CompanyProcedureController(CompanyProcedureService service) {
        this.service = service;
    }

    // ================= CREATE =================
    @PostMapping
    @ApiMessage("Create company procedure")
    public ResponseEntity<CompanyProcedureResponse> create(
            @RequestBody CompanyProcedureRequest request) {

        CompanyProcedure entity = service.handleCreate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.convertToResponse(entity));
    }

    // ================= UPDATE (THÊM MỚI) =================
    @PutMapping("/{id}")
    @ApiMessage("Update company procedure")
    public ResponseEntity<CompanyProcedureResponse> update(
            @PathVariable Long id,
            @RequestBody CompanyProcedureRequest request) {

        CompanyProcedure entity = service.handleUpdate(id, request);
        return ResponseEntity.ok(service.convertToResponse(entity));
    }

    // ================= GET ALL COMPANY =================
    @GetMapping
    @ApiMessage("Get all company procedures")
    public ResponseEntity<List<CompanyProcedureResponse>> getAllCompany() {
        return ResponseEntity.ok(service.fetchAllCompany());
    }

    // ================= GET BY DEPARTMENT =================
    @GetMapping("/by-department/{departmentId}")
    @ApiMessage("Get company procedures by department")
    public ResponseEntity<List<CompanyProcedureResponse>> getByDepartment(
            @PathVariable Long departmentId) {

        return ResponseEntity.ok(service.fetchByDepartment(departmentId));
    }

    // ================= GET BY SECTION =================
    @GetMapping("/by-section/{sectionId}")
    @ApiMessage("Get company procedures by section")
    public ResponseEntity<List<CompanyProcedureResponse>> getBySection(
            @PathVariable Long sectionId) {

        return ResponseEntity.ok(service.fetchBySection(sectionId));
    }

    // ================= GET BY ID =================
    @GetMapping("/{id}")
    @ApiMessage("Get company procedure detail")
    public ResponseEntity<CompanyProcedureResponse> getById(
            @PathVariable Long id) {

        CompanyProcedure entity = service.fetchById(id);
        if (entity == null) {
            throw new IdInvalidException("Quy trình không tồn tại");
        }

        return ResponseEntity.ok(service.convertToResponse(entity));
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    @ApiMessage("Delete company procedure")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        CompanyProcedure current = service.fetchById(id);
        if (current == null) {
            throw new IdInvalidException("Quy trình không tồn tại");
        }

        service.handleDelete(id);
        return ResponseEntity.ok(null);
    }
}
