package vn.system.app.modules.documentcategory.controller;

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
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.documentcategory.domain.DocumentCategory;
import vn.system.app.modules.documentcategory.domain.request.DocumentCategoryRequest;
import vn.system.app.modules.documentcategory.domain.response.ResDocumentCategoryDTO;
import vn.system.app.modules.documentcategory.service.DocumentCategoryService;

@RestController
@RequestMapping("/api/v1/document-categories")
@RequiredArgsConstructor
public class DocumentCategoryController {

    private final DocumentCategoryService service;

    // =====================================================
    // CREATE
    // =====================================================
    @PostMapping
    @ApiMessage("Tạo danh mục loại văn bản")
    public ResponseEntity<ResDocumentCategoryDTO> create(
            @Valid @RequestBody DocumentCategoryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.handleCreate(req));
    }

    // =====================================================
    // UPDATE
    // =====================================================
    @PutMapping("/{id}")
    @ApiMessage("Cập nhật danh mục loại văn bản")
    public ResponseEntity<ResDocumentCategoryDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody DocumentCategoryRequest req) {
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
    // GET ONE
    // =====================================================
    @GetMapping("/{id}")
    @ApiMessage("Chi tiết danh mục loại văn bản")
    public ResponseEntity<ResDocumentCategoryDTO> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(service.convertToDTO(service.fetchById(id)));
    }

    // =====================================================
    // GET ALL (paginated + filter)
    // =====================================================
    @GetMapping
    @ApiMessage("Danh sách danh mục loại văn bản")
    public ResponseEntity<ResultPaginationDTO> getAll(
            @Filter Specification<DocumentCategory> spec,
            Pageable pageable) {
        return ResponseEntity.ok(service.fetchAll(spec, pageable));
    }

    // =====================================================
    // GET ALL ACTIVE (dropdown)
    // =====================================================
    @GetMapping("/active")
    @ApiMessage("Danh sách danh mục đang hoạt động")
    public ResponseEntity<List<ResDocumentCategoryDTO>> getAllActive() {
        return ResponseEntity.ok(service.fetchAllActive());
    }

    // =====================================================
    // DELETE
    // =====================================================
    @DeleteMapping("/{id}")
    @ApiMessage("Xóa danh mục loại văn bản")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.handleDelete(id);
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // GET MAPPING PROCEDURE (dùng khi render form tạo văn bản)
    // =====================================================
    @GetMapping("/mapping-procedure")
    @ApiMessage("Danh sách danh mục có mapping quy trình")
    public ResponseEntity<List<ResDocumentCategoryDTO>> getMappingProcedure() {
        return ResponseEntity.ok(service.fetchMappingProcedure());
    }
}