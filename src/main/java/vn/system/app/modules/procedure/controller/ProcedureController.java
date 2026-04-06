package vn.system.app.modules.procedure.controller;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.ScopeSpec;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.common.util.annotation.ApiMessage;

import vn.system.app.modules.procedure.enums.ProcedureType;

import vn.system.app.modules.companyprocedure.domain.CompanyProcedure;
import vn.system.app.modules.companyprocedure.domain.response.ResCompanyProcedureDTO;
import vn.system.app.modules.companyprocedure.service.CompanyProcedureService;

import vn.system.app.modules.departmentprocedure.domain.DepartmentProcedure;
import vn.system.app.modules.departmentprocedure.domain.response.ResDepartmentProcedureDTO;
import vn.system.app.modules.departmentprocedure.service.DepartmentProcedureService;

import vn.system.app.modules.confidentialprocedure.domain.ConfidentialProcedure;
import vn.system.app.modules.confidentialprocedure.domain.response.ResConfidentialProcedureDTO;
import vn.system.app.modules.confidentialprocedure.service.ConfidentialProcedureService;

@RestController
@RequestMapping("/api/v1/procedures")
@RequiredArgsConstructor
public class ProcedureController {

    private final CompanyProcedureService companyProcedureService;
    private final DepartmentProcedureService departmentProcedureService;
    private final ConfidentialProcedureService confidentialProcedureService;

    // =====================================================
    // HELPER — check permission theo type + action
    // Sinh ra: PROCEDURE_COMPANY_UPDATE, PROCEDURE_DEPARTMENT_DELETE, ...
    // =====================================================
    private void checkPermissionByType(ProcedureType type, String action) {
        String permission = "PROCEDURE_" + type.name() + "_" + action;
        SecurityUtil.checkPermission(permission);
    }

    // =====================================================
    // CREATE
    // =====================================================
    @PostMapping
    @ApiMessage("Tạo quy trình")
    public ResponseEntity<?> create(
            @RequestParam ProcedureType type,
            @Valid @RequestBody ProcedureRequestWrapper req) {

        if (type == ProcedureType.COMPANY) {
            ResCompanyProcedureDTO res = companyProcedureService.handleCreate(req.toCompanyRequest());
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } else if (type == ProcedureType.DEPARTMENT) {
            ResDepartmentProcedureDTO res = departmentProcedureService.handleCreate(req.toDepartmentRequest());
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } else {
            ResConfidentialProcedureDTO res = confidentialProcedureService.handleCreate(req.toConfidentialRequest());
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        }
    }

    // =====================================================
    // UPDATE
    // =====================================================
    @PutMapping("/{id}")
    @ApiMessage("Cập nhật quy trình")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestParam ProcedureType type,
            @Valid @RequestBody ProcedureRequestWrapper req) {

        checkPermissionByType(type, "UPDATE");

        if (type == ProcedureType.COMPANY) {
            return ResponseEntity.ok(companyProcedureService.handleUpdate(id, req.toCompanyRequest()));
        } else if (type == ProcedureType.DEPARTMENT) {
            return ResponseEntity.ok(departmentProcedureService.handleUpdate(id, req.toDepartmentRequest()));
        } else {
            return ResponseEntity.ok(confidentialProcedureService.handleUpdate(id, req.toConfidentialRequest()));
        }
    }

    // =====================================================
    // REVISE
    // =====================================================
    @PostMapping("/{id}/revise")
    @ApiMessage("Tạo phiên bản mới")
    public ResponseEntity<?> revise(
            @PathVariable Long id,
            @RequestParam ProcedureType type,
            @Valid @RequestBody ProcedureRequestWrapper req) {

        checkPermissionByType(type, "REVISE");

        if (type == ProcedureType.COMPANY) {
            return ResponseEntity.ok(companyProcedureService.handleRevise(id, req.toCompanyRequest()));
        } else if (type == ProcedureType.DEPARTMENT) {
            return ResponseEntity.ok(departmentProcedureService.handleRevise(id, req.toDepartmentRequest()));
        } else {
            return ResponseEntity.ok(confidentialProcedureService.handleRevise(id, req.toConfidentialRequest()));
        }
    }

    // =====================================================
    // TOGGLE ACTIVE
    // =====================================================
    @PutMapping("/{id}/active")
    @ApiMessage("Thay đổi trạng thái kích hoạt")
    public ResponseEntity<Void> toggleActive(
            @PathVariable Long id,
            @RequestParam ProcedureType type) {

        if (type == ProcedureType.COMPANY) {
            companyProcedureService.handleToggleActive(id);
        } else if (type == ProcedureType.DEPARTMENT) {
            departmentProcedureService.handleToggleActive(id);
        } else {
            confidentialProcedureService.handleToggleActive(id);
        }
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // DELETE
    // =====================================================
    @DeleteMapping("/{id}")
    @ApiMessage("Xoá quy trình")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam ProcedureType type) {

        checkPermissionByType(type, "DELETE");

        if (type == ProcedureType.COMPANY) {
            companyProcedureService.handleDelete(id);
        } else if (type == ProcedureType.DEPARTMENT) {
            departmentProcedureService.handleDelete(id);
        } else {
            confidentialProcedureService.handleDelete(id);
        }
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // GET ONE
    // =====================================================
    @GetMapping("/{id}")
    @ApiMessage("Chi tiết quy trình")
    public ResponseEntity<?> getOne(
            @PathVariable Long id,
            @RequestParam ProcedureType type) {

        checkPermissionByType(type, "GET_BY_ID");

        if (type == ProcedureType.COMPANY) {
            return ResponseEntity.ok(companyProcedureService.convertToDTO(
                    companyProcedureService.fetchById(id)));
        } else if (type == ProcedureType.DEPARTMENT) {
            return ResponseEntity.ok(departmentProcedureService.convertToDTO(
                    departmentProcedureService.fetchById(id)));
        } else {
            confidentialProcedureService.checkAccess(id);
            return ResponseEntity.ok(confidentialProcedureService.convertToDTO(
                    confidentialProcedureService.fetchById(id)));
        }
    }

    // =====================================================
    // GET ALL
    // =====================================================
    @GetMapping
    @ApiMessage("Danh sách quy trình")
    public ResponseEntity<ResultPaginationDTO> getAll(
            @RequestParam ProcedureType type,
            Pageable pageable) {

        if (type == ProcedureType.COMPANY) {
            Specification<CompanyProcedure> spec = ScopeSpec.byCompanyScope("department.company.id");
            return ResponseEntity.ok(companyProcedureService.fetchAll(spec, pageable));
        } else if (type == ProcedureType.DEPARTMENT) {
            Specification<DepartmentProcedure> spec = ScopeSpec.byCompanyScope("department.company.id");
            return ResponseEntity.ok(departmentProcedureService.fetchAll(spec, pageable));
        } else {
            Specification<ConfidentialProcedure> spec = ScopeSpec.byCompanyScope("department.company.id");
            return ResponseEntity.ok(confidentialProcedureService.fetchAll(spec, pageable));
        }
    }

    // =====================================================
    // GET ALL COMPANY — có @Filter
    // =====================================================
    @GetMapping("/company")
    @ApiMessage("Danh sách quy trình công ty có filter")
    public ResponseEntity<ResultPaginationDTO> getAllCompany(
            @Filter Specification<CompanyProcedure> spec,
            Pageable pageable) {

        spec = spec.and(ScopeSpec.byCompanyScope("department.company.id"));
        return ResponseEntity.ok(companyProcedureService.fetchAll(spec, pageable));
    }

    // =====================================================
    // GET ALL DEPARTMENT — có @Filter
    // =====================================================
    @GetMapping("/department")
    @ApiMessage("Danh sách quy trình phòng ban có filter")
    public ResponseEntity<ResultPaginationDTO> getAllDepartment(
            @Filter Specification<DepartmentProcedure> spec,
            Pageable pageable) {

        spec = spec.and(ScopeSpec.byCompanyScope("department.company.id"));
        return ResponseEntity.ok(departmentProcedureService.fetchAll(spec, pageable));
    }

    // =====================================================
    // GET ALL CONFIDENTIAL — có @Filter
    // =====================================================
    @GetMapping("/confidential")
    @ApiMessage("Danh sách quy trình bảo mật có filter")
    public ResponseEntity<ResultPaginationDTO> getAllConfidential(
            @Filter Specification<ConfidentialProcedure> spec,
            Pageable pageable) {

        spec = spec.and(ScopeSpec.byCompanyScope("department.company.id"));
        return ResponseEntity.ok(confidentialProcedureService.fetchAll(spec, pageable));
    }

    // =====================================================
    // GET BY COMPANY
    // =====================================================
    @GetMapping("/by-company/{companyId}")
    @ApiMessage("Danh sách quy trình theo công ty")
    public ResponseEntity<List<?>> getByCompany(
            @PathVariable Long companyId,
            @RequestParam ProcedureType type) {

        if (type == ProcedureType.COMPANY) {
            return ResponseEntity.ok(companyProcedureService.fetchByCompany(companyId));
        } else if (type == ProcedureType.DEPARTMENT) {
            return ResponseEntity.ok(departmentProcedureService.fetchByCompany(companyId));
        } else {
            return ResponseEntity.ok(confidentialProcedureService.fetchByCompany(companyId));
        }
    }

    // =====================================================
    // GET BY DEPARTMENT
    // =====================================================
    @GetMapping("/by-department/{departmentId}")
    @ApiMessage("Danh sách quy trình theo phòng ban")
    public ResponseEntity<List<?>> getByDepartment(
            @PathVariable Long departmentId,
            @RequestParam ProcedureType type) {

        if (type == ProcedureType.COMPANY) {
            return ResponseEntity.ok(companyProcedureService.fetchByDepartment(departmentId));
        } else if (type == ProcedureType.DEPARTMENT) {
            return ResponseEntity.ok(departmentProcedureService.fetchByDepartment(departmentId));
        } else {
            return ResponseEntity.ok(confidentialProcedureService.fetchByDepartment(departmentId));
        }
    }

    // =====================================================
    // GET BY SECTION
    // =====================================================
    @GetMapping("/by-section/{sectionId}")
    @ApiMessage("Danh sách quy trình theo bộ phận")
    public ResponseEntity<List<?>> getBySection(
            @PathVariable Long sectionId,
            @RequestParam ProcedureType type) {

        if (type == ProcedureType.COMPANY) {
            return ResponseEntity.ok(companyProcedureService.fetchBySection(sectionId));
        } else if (type == ProcedureType.DEPARTMENT) {
            return ResponseEntity.ok(departmentProcedureService.fetchBySection(sectionId));
        } else {
            return ResponseEntity.ok(confidentialProcedureService.fetchBySection(sectionId));
        }
    }

    // =====================================================
    // GET HISTORY
    // =====================================================
    @GetMapping("/{id}/history")
    @ApiMessage("Lịch sử quy trình")
    public ResponseEntity<List<?>> getHistory(
            @PathVariable Long id,
            @RequestParam ProcedureType type) {

        if (type == ProcedureType.COMPANY) {
            return ResponseEntity.ok(companyProcedureService.fetchHistory(id));
        } else if (type == ProcedureType.DEPARTMENT) {
            return ResponseEntity.ok(departmentProcedureService.fetchHistory(id));
        } else {
            confidentialProcedureService.checkAccess(id);
            return ResponseEntity.ok(confidentialProcedureService.fetchHistory(id));
        }
    }

    // =====================================================
    // CREATE COMPANY
    // =====================================================
    @PostMapping("/company")
    @ApiMessage("Tạo quy trình công ty")
    public ResponseEntity<?> createCompany(
            @Valid @RequestBody ProcedureRequestWrapper req) {
        ResCompanyProcedureDTO res = companyProcedureService.handleCreate(req.toCompanyRequest());
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // =====================================================
    // CREATE DEPARTMENT
    // =====================================================
    @PostMapping("/department")
    @ApiMessage("Tạo quy trình phòng ban")
    public ResponseEntity<?> createDepartment(
            @Valid @RequestBody ProcedureRequestWrapper req) {
        ResDepartmentProcedureDTO res = departmentProcedureService.handleCreate(req.toDepartmentRequest());
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    // =====================================================
    // CREATE CONFIDENTIAL
    // =====================================================
    @PostMapping("/confidential")
    @ApiMessage("Tạo quy trình bảo mật")
    public ResponseEntity<?> createConfidential(
            @Valid @RequestBody ProcedureRequestWrapper req) {
        ResConfidentialProcedureDTO res = confidentialProcedureService.handleCreate(req.toConfidentialRequest());
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }
}