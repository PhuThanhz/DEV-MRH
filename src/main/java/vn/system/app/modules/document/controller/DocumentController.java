package vn.system.app.modules.document.controller;

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
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.document.domain.Document;
import vn.system.app.modules.document.domain.request.DocumentRequest;
import vn.system.app.modules.document.domain.response.ResDocumentDTO;
import vn.system.app.modules.document.service.DocumentService;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService service;

    // =====================================================
    // CREATE
    // =====================================================
    @PostMapping
    @ApiMessage("Tạo văn bản")
    public ResponseEntity<ResDocumentDTO> create(
            @Valid @RequestBody DocumentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.handleCreate(req));
    }

    // =====================================================
    // UPDATE
    // =====================================================
    @PutMapping("/{id}")
    @ApiMessage("Cập nhật văn bản")
    public ResponseEntity<ResDocumentDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody DocumentRequest req) {
        return ResponseEntity.ok(service.handleUpdate(id, req));
    }

    // =====================================================
    // TOGGLE ACTIVE
    // =====================================================
    @PutMapping("/{id}/active")
    @ApiMessage("Thay đổi trạng thái kích hoạt")
    public ResponseEntity<Void> toggleActive(@PathVariable Long id) {
        service.handleToggleActive(id);
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // DELETE
    // =====================================================
    @DeleteMapping("/{id}")
    @ApiMessage("Xoá văn bản")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.handleDelete(id);
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // GET ONE
    // =====================================================
    @GetMapping("/{id}")
    @ApiMessage("Chi tiết văn bản")
    public ResponseEntity<ResDocumentDTO> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(service.convertToDTO(service.fetchById(id)));
    }

    // =====================================================
    // GET ALL (filter + paginate)
    // =====================================================
    @GetMapping
    @ApiMessage("Danh sách văn bản")
    public ResponseEntity<ResultPaginationDTO> getAll(
            @Filter Specification<Document> spec,
            Pageable pageable) {
        spec = spec.and(ScopeSpec.byCompanyScope("department.company.id"));
        return ResponseEntity.ok(service.fetchAll(spec, pageable));
    }

    // =====================================================
    // GET BY COMPANY
    // =====================================================
    @GetMapping("/by-company/{companyId}")
    @ApiMessage("Danh sách văn bản theo công ty")
    public ResponseEntity<List<ResDocumentDTO>> getByCompany(
            @PathVariable Long companyId) {
        return ResponseEntity.ok(service.fetchByCompany(companyId));
    }

    // =====================================================
    // GET BY DEPARTMENT
    // =====================================================
    @GetMapping("/by-department/{departmentId}")
    @ApiMessage("Danh sách văn bản theo phòng ban")
    public ResponseEntity<List<ResDocumentDTO>> getByDepartment(
            @PathVariable Long departmentId) {
        return ResponseEntity.ok(service.fetchByDepartment(departmentId));
    }

    // =====================================================
    // GET BY SECTION
    // =====================================================
    @GetMapping("/by-section/{sectionId}")
    @ApiMessage("Danh sách văn bản theo bộ phận")
    public ResponseEntity<List<ResDocumentDTO>> getBySection(
            @PathVariable Long sectionId) {
        return ResponseEntity.ok(service.fetchBySection(sectionId));
    }

    // =====================================================
    // GET BY CATEGORY
    // =====================================================
    @GetMapping("/by-category/{categoryId}")
    @ApiMessage("Danh sách văn bản theo loại")
    public ResponseEntity<List<ResDocumentDTO>> getByCategory(
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(service.fetchByCategory(categoryId));
    }
}