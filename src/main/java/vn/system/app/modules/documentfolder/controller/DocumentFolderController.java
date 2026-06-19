package vn.system.app.modules.documentfolder.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.documentfolder.domain.request.DocumentFolderRequest;
import vn.system.app.modules.documentfolder.domain.response.ResDocumentFolderDTO;
import vn.system.app.modules.documentfolder.service.DocumentFolderService;
import vn.system.app.modules.document.domain.response.ResDocumentDTO;
import vn.system.app.modules.user.domain.response.ResUserDTO;

@RestController
@RequestMapping("/api/v1/folders")
@RequiredArgsConstructor
public class DocumentFolderController {

    private final DocumentFolderService service;

    @GetMapping("/tree")
    @ApiMessage("Lấy cây thư mục")
    public ResponseEntity<List<ResDocumentFolderDTO>> getTree(
            @RequestParam(required = false) String ownerId) {
        String id = ownerId != null ? ownerId : SecurityUtil.getCurrentUserId().orElse("");
        return ResponseEntity.ok(service.getTreeForUser(id));
    }

    @GetMapping("/accounting/tree")
    @ApiMessage("Lấy cây thư mục Kế toán")
    public ResponseEntity<List<ResDocumentFolderDTO>> getAccountingTree(
            @RequestParam Long companyId) {
        return ResponseEntity.ok(service.getAccountingTree(companyId));
    }

    @PostMapping
    @ApiMessage("Tạo thư mục mới")
    public ResponseEntity<ResDocumentFolderDTO> create(
            @Valid @RequestBody DocumentFolderRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createFolder(req));
    }

    @PutMapping("/{id}")
    @ApiMessage("Cập nhật thư mục")
    public ResponseEntity<ResDocumentFolderDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody DocumentFolderRequest req) {
        return ResponseEntity.ok(service.updateFolder(id, req));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Xóa thư mục")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteFolder(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/subordinates")
    @ApiMessage("Danh sách cấp dưới trực tiếp")
    public ResponseEntity<List<ResUserDTO>> getSubordinates() {
        return ResponseEntity.ok(service.getSubordinates());
    }

    @GetMapping("/{id}/documents")
    @ApiMessage("Lấy danh sách tài liệu trong thư mục")
    public ResponseEntity<List<ResDocumentDTO>> getFolderDocuments(@PathVariable Long id) {
        return ResponseEntity.ok(service.getFolderDocuments(id));
    }
}
