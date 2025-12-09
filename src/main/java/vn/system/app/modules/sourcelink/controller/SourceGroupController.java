package vn.system.app.modules.sourcelink.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.sourcelink.domain.SourceGroup;
import vn.system.app.modules.sourcelink.domain.request.ReqAddLinkDTO;
import vn.system.app.modules.sourcelink.domain.request.ReqCreateGroupDTO;
import vn.system.app.modules.sourcelink.domain.response.ResSourceGroupDTO;
import vn.system.app.modules.sourcelink.service.SourceGroupService;

@RestController
@RequestMapping("/api/v1/source-groups")
public class SourceGroupController {

    private final SourceGroupService groupService;

    public SourceGroupController(SourceGroupService groupService) {
        this.groupService = groupService;
    }

    // ============================================================
    // 1 Tạo group mới (chưa có link)
    // ============================================================
    @PostMapping
    @ApiMessage("Tạo group mới (chưa có link)")
    public ResponseEntity<ResSourceGroupDTO> create(@Valid @RequestBody ReqCreateGroupDTO req) {
        SourceGroup created = groupService.handleCreate(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.convertToResDTO(created));
    }

    // ============================================================
    // 2 Danh sách tất cả group (phân trang)
    // ============================================================
    @GetMapping
    @ApiMessage("Danh sách tất cả group (phân trang)")
    public ResponseEntity<ResultPaginationDTO> getAll(Pageable pageable) {
        return ResponseEntity.ok(groupService.handleGetAll(pageable));
    }

    // ============================================================
    // 4 Cập nhật tên group
    // ============================================================
    @PutMapping("/{id}")
    @ApiMessage("Cập nhật tên group")
    public ResponseEntity<ResSourceGroupDTO> updateName(
            @PathVariable("id") Long id,
            @RequestBody String newName) {

        if (newName == null || newName.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        SourceGroup updated = groupService.handleUpdateName(id, newName);
        return ResponseEntity.ok(groupService.convertToResDTO(updated));
    }

    // ============================================================
    // 5 Xóa group (và toàn bộ link bên trong)
    // ============================================================
    @DeleteMapping("/{id}")
    @ApiMessage("Xóa group (và toàn bộ link bên trong)")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        groupService.handleDelete(id);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // 6 Thêm link mới vào group (dùng DTO)
    // ============================================================
    @PostMapping("/{groupId}/links")
    @ApiMessage("Thêm link mới vào group")
    public ResponseEntity<ResSourceGroupDTO> addLink(
            @PathVariable("groupId") Long groupId,
            @Valid @RequestBody ReqAddLinkDTO req) {

        SourceGroup updated = groupService.handleAddLink(groupId, req.getUrl());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.convertToResDTO(updated));
    }

    // ============================================================
    // 7 Xóa 1 link khỏi group
    // ============================================================
    @DeleteMapping("/{groupId}/links/{linkId}")
    @ApiMessage("Xóa 1 link khỏi group")
    public ResponseEntity<ResSourceGroupDTO> deleteLink(
            @PathVariable("groupId") Long groupId,
            @PathVariable("linkId") Long linkId) {

        SourceGroup updated = groupService.handleDeleteLink(groupId, linkId);
        return ResponseEntity.ok(groupService.convertToResDTO(updated));
    }
}
