package vn.system.app.modules.sourcelink.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.sourcelink.domain.SourceGroup;
import vn.system.app.modules.sourcelink.domain.request.ReqAddLinkDTO;
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
    // 1. CẬP NHẬT NHÓM CON
    // ============================================================
    @PutMapping
    @ApiMessage("Cập nhật thông tin nhóm con")
    public ResponseEntity<ResSourceGroupDTO> update(@Valid @RequestBody SourceGroup req) {
        SourceGroup updated = groupService.handleUpdate(req);
        return ResponseEntity.ok(groupService.convertToDTO(updated));
    }

    // ============================================================
    // 2. XÓA NHÓM (VÀ TOÀN BỘ LINK BÊN TRONG)
    // ============================================================
    @DeleteMapping("/{id}")
    @ApiMessage("Xóa nhóm (và toàn bộ link bên trong)")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        groupService.handleDelete(id);
        return ResponseEntity.ok(null);
    }

    // ============================================================
    // 3. THÊM LINK MỚI VÀO NHÓM
    // ============================================================
    @PostMapping("/{groupId}/links")
    @ApiMessage("Thêm link mới vào nhóm")
    public ResponseEntity<ResSourceGroupDTO> addLink(
            @PathVariable("groupId") Long groupId,
            @Valid @RequestBody ReqAddLinkDTO req) {

        SourceGroup updated = groupService.handleAddLink(groupId, req.getUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.convertToDTO(updated));
    }

    // ============================================================
    // 4. XÓA 1 LINK KHỎI NHÓM
    // ============================================================
    @DeleteMapping("/{groupId}/links/{linkId}")
    @ApiMessage("Xóa 1 link khỏi nhóm")
    public ResponseEntity<ResSourceGroupDTO> deleteLink(
            @PathVariable("groupId") Long groupId,
            @PathVariable("linkId") Long linkId) {

        SourceGroup updated = groupService.handleDeleteLink(groupId, linkId);
        return ResponseEntity.ok(groupService.convertToDTO(updated));
    }
}
