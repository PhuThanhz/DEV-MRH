package vn.system.app.modules.permissioncontent.controller;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.permissioncontent.domain.PermissionContent;
import vn.system.app.modules.permissioncontent.domain.request.ReqCreatePermissionContentDTO;
import vn.system.app.modules.permissioncontent.domain.request.ReqUpdatePermissionContentDTO;
import vn.system.app.modules.permissioncontent.domain.response.ResPermissionContentDetailDTO;
import vn.system.app.modules.permissioncontent.service.PermissionContentService;

@RestController
@RequestMapping("/api/v1/permission-contents")
public class PermissionContentController {

    private final PermissionContentService service;

    public PermissionContentController(PermissionContentService service) {
        this.service = service;
    }

    /*
     * =====================================================
     * CREATE
     * =====================================================
     */
    @PostMapping
    @ApiMessage("Tạo nội dung quyền")
    public ResponseEntity<ResPermissionContentDetailDTO> create(
            @Valid @RequestBody ReqCreatePermissionContentDTO req) {

        PermissionContent entity = service.handleCreatePermissionContent(req);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.convertToResPermissionContentDetailDTO(entity));
    }

    /*
     * =====================================================
     * GET ALL BY CATEGORY (BẮT BUỘC categoryId)
     * =====================================================
     */
    @GetMapping
    @ApiMessage("Danh sách nội dung quyền theo danh mục")
    public ResponseEntity<ResultPaginationDTO> getAll(
            @RequestParam Long categoryId,
            Pageable pageable) {

        return ResponseEntity.ok(
                service.fetchAllPermissionContent(categoryId, pageable));
    }

    /*
     * =====================================================
     * GET DETAIL
     * =====================================================
     */
    @GetMapping("/{id}")
    @ApiMessage("Chi tiết nội dung quyền")
    public ResponseEntity<ResPermissionContentDetailDTO> detail(
            @PathVariable Long id) {

        PermissionContent entity = service.fetchPermissionContentById(id);
        return ResponseEntity.ok(
                service.convertToResPermissionContentDetailDTO(entity));
    }

    /*
     * =====================================================
     * UPDATE
     * =====================================================
     */
    @PutMapping("/{id}")
    @ApiMessage("Cập nhật nội dung quyền")
    public ResponseEntity<ResPermissionContentDetailDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ReqUpdatePermissionContentDTO req) {

        PermissionContent entity = service.handleUpdatePermissionContent(id, req);
        return ResponseEntity.ok(
                service.convertToResPermissionContentDetailDTO(entity));
    }

    /*
     * =====================================================
     * DELETE (SOFT)
     * =====================================================
     */
    @DeleteMapping("/{id}")
    @ApiMessage("Xoá (soft) nội dung quyền")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        service.handleDeletePermissionContent(id);
        return ResponseEntity.ok().build();
    }

    /*
     * =====================================================
     * TOGGLE ACTIVE
     * =====================================================
     */
    @PatchMapping("/{id}/toggle")
    @ApiMessage("Bật / tắt nội dung quyền")
    public ResponseEntity<Void> toggle(@PathVariable Long id) {

        service.handleTogglePermissionContent(id);
        return ResponseEntity.ok().build();
    }
}
