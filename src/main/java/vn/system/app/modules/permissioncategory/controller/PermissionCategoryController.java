package vn.system.app.modules.permissioncategory.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.permissioncategory.domain.PermissionCategory;
import vn.system.app.modules.permissioncategory.domain.request.PermissionCategoryRequest;
import vn.system.app.modules.permissioncategory.domain.response.PermissionCategoryResponse;
import vn.system.app.modules.permissioncategory.service.PermissionCategoryService;

@RestController
@RequestMapping("/api/v1/permission-categories")
public class PermissionCategoryController {

    private final PermissionCategoryService service;

    public PermissionCategoryController(PermissionCategoryService service) {
        this.service = service;
    }

    // =====================================================
    // CREATE
    // =====================================================
    @PostMapping
    @ApiMessage("Tạo danh mục phân quyền")
    public ResponseEntity<PermissionCategoryResponse> create(
            @Valid @RequestBody PermissionCategoryRequest req) {

        PermissionCategory entity = service.handleCreateCategory(req);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.convertToResponse(entity));
    }

    // =====================================================
    // UPDATE
    // =====================================================
    @PutMapping("/{id}")
    @ApiMessage("Cập nhật danh mục phân quyền")
    public ResponseEntity<PermissionCategoryResponse> update(
            @PathVariable long id,
            @Valid @RequestBody PermissionCategoryRequest req) {

        PermissionCategory entity = service.handleUpdateCategory(id, req);
        return ResponseEntity.ok(service.convertToResponse(entity));
    }

    // =====================================================
    // GET ALL (PAGINATION)
    // =====================================================
    @GetMapping
    @ApiMessage("Danh sách danh mục phân quyền (phân trang)")
    public ResponseEntity<ResultPaginationDTO> getAll(Pageable pageable) {
        return ResponseEntity.ok(service.fetchAllCategory(pageable));
    }

    // =====================================================
    // GET BY ID
    // =====================================================
    @GetMapping("/{id}")
    @ApiMessage("Chi tiết danh mục phân quyền")
    public ResponseEntity<PermissionCategoryResponse> getDetail(
            @PathVariable long id) {

        PermissionCategory entity = service.fetchCategoryById(id);
        return ResponseEntity.ok(service.convertToResponse(entity));
    }

    // =====================================================
    // DELETE (SOFT)
    // =====================================================
    @DeleteMapping("/{id}")
    @ApiMessage("Ngưng sử dụng danh mục phân quyền")
    public ResponseEntity<Void> delete(@PathVariable long id) {

        service.handleDeleteCategory(id);
        return ResponseEntity.ok().build();
    }
}
