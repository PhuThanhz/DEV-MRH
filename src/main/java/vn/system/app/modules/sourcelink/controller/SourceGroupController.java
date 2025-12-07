package vn.system.app.modules.sourcelink.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.sourcelink.domain.SourceGroup;
import vn.system.app.modules.sourcelink.domain.SourceLink;
import vn.system.app.modules.sourcelink.domain.reponse.ResSourceGroupDTO;
import vn.system.app.modules.sourcelink.domain.request.ReqCreateGroupDTO;
import vn.system.app.modules.sourcelink.service.SourceGroupService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/source-groups")
public class SourceGroupController {

    private final SourceGroupService groupService;

    public SourceGroupController(SourceGroupService groupService) {
        this.groupService = groupService;
    }

    // ============================================================
    // 1️ Tạo group mới
    // ============================================================
    @PostMapping
    @ApiMessage("Tạo group mới với 1 link khởi tạo")
    public ResponseEntity<ResSourceGroupDTO> createGroup(@Valid @RequestBody ReqCreateGroupDTO req) {
        String firstUrl = req.getUrls().get(0);
        SourceGroup group = groupService.createGroup(req.getGroupName(), firstUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(group));
    }

    // ============================================================
    // 2️ Thêm 1 link mới vào group hiện có
    // ============================================================
    @PostMapping("/{groupId}/links")
    @ApiMessage("Thêm 1 link mới vào group hiện có")
    public ResponseEntity<?> addLinkToGroup(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> body) {

        String url = body.get("url") != null ? body.get("url").toString().trim() : "";
        if (url.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "URL không được để trống"));
        }

        try {
            SourceLink link = groupService.addLinkToGroup(groupId, url);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Thêm link thành công",
                    "groupId", groupId,
                    "linkId", link.getId(),
                    "url", link.getUrl()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // ============================================================
    // 3️ Lấy danh sách tất cả group (kèm tổng số link)
    // ============================================================
    @GetMapping
    @ApiMessage("Lấy danh sách tất cả group (kèm tổng số link)")
    public ResponseEntity<List<ResSourceGroupDTO>> getAllGroups() {
        List<ResSourceGroupDTO> response = groupService.getAllGroups().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 4️ Lấy chi tiết 1 group (kèm danh sách link)
    // ============================================================
    @GetMapping("/{groupId}")
    @ApiMessage("Lấy chi tiết group (kèm danh sách link)")
    public ResponseEntity<?> getGroupDetail(@PathVariable Long groupId) {
        try {
            SourceGroup group = groupService.getGroup(groupId);
            return ResponseEntity.ok(convertToDTO(group));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // ============================================================
    // 5️ Cập nhật tên group
    // ============================================================
    @PutMapping("/{groupId}")
    @ApiMessage("Cập nhật tên group")
    public ResponseEntity<?> updateGroupName(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> body) {

        String newName = body.get("name") != null ? body.get("name").toString().trim() : "";
        if (newName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tên group không được để trống"));
        }

        try {
            SourceGroup updated = groupService.rename(groupId, newName);
            return ResponseEntity.ok(convertToDTO(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // ============================================================
    // 6️ Xóa group (và toàn bộ link bên trong)
    // ============================================================
    @DeleteMapping("/{groupId}")
    @ApiMessage("Xóa group và toàn bộ link bên trong")
    public ResponseEntity<?> deleteGroup(@PathVariable Long groupId) {
        try {
            groupService.deleteGroup(groupId);
            return ResponseEntity.ok(Map.of("message", "Đã xóa group ID = " + groupId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // ============================================================
    // 7️ Xóa 1 link khỏi group
    // ============================================================
    @DeleteMapping("/{groupId}/links/{linkId}")
    @ApiMessage("Xóa 1 link khỏi group")
    public ResponseEntity<?> deleteLinkFromGroup(
            @PathVariable Long groupId,
            @PathVariable Long linkId) {
        try {
            groupService.deleteLink(groupId, linkId);
            return ResponseEntity.ok(Map.of("message", "Đã xóa link ID = " + linkId + " khỏi group ID = " + groupId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // ============================================================
    // Helper: convert entity → DTO
    // ============================================================
    private ResSourceGroupDTO convertToDTO(SourceGroup group) {
        ResSourceGroupDTO dto = new ResSourceGroupDTO();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setCreatedAt(group.getCreatedAt());
        dto.setUpdatedAt(group.getUpdatedAt());
        dto.setTotalLinks(group.getLinks() != null ? group.getLinks().size() : 0);

        if (group.getLinks() != null) {
            List<ResSourceGroupDTO.LinkInfo> links = group.getLinks().stream()
                    .map(link -> new ResSourceGroupDTO.LinkInfo(
                            link.getId(),
                            link.getUrl(),
                            link.getCaption(),
                            link.getStatus() != null ? link.getStatus().name() : null,
                            link.getContentGenerated(),
                            link.getErrorMessage()))
                    .collect(Collectors.toList());
            dto.setLinks(links);
        }
        return dto;
    }
}
