package vn.system.app.modules.sourcelink.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.sourcelink.domain.SourceLink;
import vn.system.app.modules.sourcelink.domain.request.ReqUpdateCaptionDTO;
import vn.system.app.modules.sourcelink.service.SourceLinkService;
import org.springframework.data.domain.Pageable;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/source-links")
public class SourceLinkController {

    private final SourceLinkService linkService;

    public SourceLinkController(SourceLinkService linkService) {
        this.linkService = linkService;
    }

    @PostMapping("/{groupId}/process")
    @ApiMessage("Xử lý tải toàn bộ link trong group")
    public ResponseEntity<Map<String, Object>> processGroup(@PathVariable("groupId") Long groupId) {
        linkService.handleProcessGroup(groupId);
        return ResponseEntity.ok(Map.of(
                "message", "Đang xử lý tải group ID = " + groupId));
    }

    @GetMapping("/group/{groupId}")
    @ApiMessage("Lấy danh sách link trong group")
    public ResponseEntity<ResultPaginationDTO> getLinksByGroup(
            @PathVariable("groupId") Long groupId,
            @RequestParam(value = "status", required = false) String status,
            Pageable pageable) {

        ResultPaginationDTO result = linkService.handleGetLinksByGroup(groupId, pageable, status);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{linkId}/caption")
    @ApiMessage("Cập nhật caption cho link")
    public ResponseEntity<SourceLink> updateCaption(
            @PathVariable("linkId") Long linkId,
            @Valid @RequestBody ReqUpdateCaptionDTO req) {

        SourceLink updated = linkService.handleUpdateCaption(linkId, req.getCaption());
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{linkId}")
    @ApiMessage("Lấy chi tiết 1 link theo ID")
    public ResponseEntity<SourceLink> getLinkDetail(@PathVariable("linkId") Long linkId) {
        SourceLink link = linkService.findById(linkId);
        return ResponseEntity.ok(link);
    }

}
