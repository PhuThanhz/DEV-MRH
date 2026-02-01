package vn.system.app.modules.companyprocedure.controller;

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
import vn.system.app.modules.companyprocedure.domain.CompanyProcedure;
import vn.system.app.modules.companyprocedure.domain.request.CompanyProcedureRequest;
import vn.system.app.modules.companyprocedure.domain.response.CompanyProcedureResponse;
import vn.system.app.modules.companyprocedure.service.CompanyProcedureService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/company-procedures")
@RequiredArgsConstructor
public class CompanyProcedureController {

    private final CompanyProcedureService service;

    /*
     * =====================================================
     * CREATE
     * =====================================================
     */
    @PostMapping
    @ApiMessage("Tạo quy trình mới")
    public ResponseEntity<CompanyProcedureResponse> create(
            @Valid @RequestBody CompanyProcedureRequest request) {

        CompanyProcedureResponse res = service.handleCreate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    /*
     * =====================================================
     * UPDATE
     * =====================================================
     */
    @PutMapping("/{id}")
    @ApiMessage("Cập nhật quy trình")
    public ResponseEntity<CompanyProcedureResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CompanyProcedureRequest request) {

        CompanyProcedureResponse res = service.handleUpdate(id, request);
        return ResponseEntity.ok(res);
    }

    /*
     * =====================================================
     * TOGGLE ACTIVE (BẬT / TẮT)
     * =====================================================
     */
    @PutMapping("/{id}/active")
    @ApiMessage("Thay đổi trạng thái kích hoạt quy trình")
    public ResponseEntity<Void> toggleActive(@PathVariable Long id) {
        service.handleToggleActive(id);
        return ResponseEntity.ok().build();
    }

    /*
     * =====================================================
     * GET ONE
     * =====================================================
     */
    @GetMapping("/{id}")
    @ApiMessage("Chi tiết quy trình")
    public ResponseEntity<CompanyProcedureResponse> getOne(@PathVariable Long id) {

        CompanyProcedure entity = service.fetchById(id);
        if (entity == null) {
            throw new IdInvalidException("Quy trình với id = " + id + " không tồn tại");
        }

        return ResponseEntity.ok(service.convertToResponse(entity));
    }

    /*
     * =====================================================
     * GET ALL (LỌC + PHÂN TRANG)
     * =====================================================
     */
    @GetMapping
    @ApiMessage("Danh sách quy trình")
    public ResponseEntity<ResultPaginationDTO> getAll(
            @Filter Specification<CompanyProcedure> spec,
            Pageable pageable) {

        return ResponseEntity.ok(service.fetchAll(spec, pageable));
    }

    /*
     * =====================================================
     * GET BY DEPARTMENT
     * =====================================================
     */
    @GetMapping("/by-department/{departmentId}")
    @ApiMessage("Danh sách quy trình theo phòng ban")
    public ResponseEntity<List<CompanyProcedureResponse>> getByDepartment(
            @PathVariable Long departmentId) {

        return ResponseEntity.ok(service.fetchByDepartment(departmentId));
    }

    /*
     * =====================================================
     * GET BY SECTION
     * =====================================================
     */
    @GetMapping("/by-section/{sectionId}")
    @ApiMessage("Danh sách quy trình theo bộ phận")
    public ResponseEntity<List<CompanyProcedureResponse>> getBySection(
            @PathVariable Long sectionId) {

        return ResponseEntity.ok(service.fetchBySection(sectionId));
    }
}
