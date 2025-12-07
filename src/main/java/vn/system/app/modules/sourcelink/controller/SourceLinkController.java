package vn.system.app.modules.sourcelink.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.sourcelink.domain.SourceLink;
import vn.system.app.modules.sourcelink.service.SourceLinkService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/source-links")
public class SourceLinkController {

    private final SourceLinkService linkService;

    public SourceLinkController(SourceLinkService linkService) {
        this.linkService = linkService;
    }

    // ============================================================
    // 1️ Xử lý tải toàn bộ link trong group
    // ============================================================
    @PostMapping("/{groupId}/process")
    @ApiMessage("Xử lý tải toàn bộ link trong group")
    public ResponseEntity<?> processGroup(@PathVariable Long groupId) {
        try {
            linkService.processGroup(groupId);
            return ResponseEntity.ok(Map.of("message", "Đang xử lý tải group ID = " + groupId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // ============================================================
    // 2 Lấy danh sách link SUCCESS
    // ============================================================
    @GetMapping("/{groupId}/success")
    @ApiMessage("Lấy danh sách link đã tải SUCCESS trong group")
    public ResponseEntity<List<SourceLink>> getSuccessLinks(@PathVariable Long groupId) {
        List<SourceLink> links = linkService.getSuccessLinksByGroup(groupId);
        return ResponseEntity.ok(links);
    }
}
