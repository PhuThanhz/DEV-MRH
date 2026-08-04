package vn.system.app.modules.task.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.task.domain.request.ReqRegisterTaskAttachmentDTO;
import vn.system.app.modules.task.domain.response.ResTaskAttachmentDTO;
import vn.system.app.modules.task.service.TaskAttachmentService;

@RestController
@RequestMapping("/api/v1")
public class TaskAttachmentController {

    private final TaskAttachmentService attachmentService;

    public TaskAttachmentController(TaskAttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping("/tasks/{id}/attachments")
    @ApiMessage("Đăng ký file đính kèm tác vụ thành công")
    public ResponseEntity<ResTaskAttachmentDTO> registerAttachment(
            @PathVariable("id") Long id,
            @Valid @RequestBody ReqRegisterTaskAttachmentDTO req) {
        ResTaskAttachmentDTO dto = attachmentService.registerGeneralAttachment(id, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/tasks/{id}/attachments")
    @ApiMessage("Danh sách file đính kèm tác vụ")
    public ResponseEntity<List<ResTaskAttachmentDTO>> fetchAttachments(
            @PathVariable("id") Long id) {

        return ResponseEntity.ok(attachmentService.fetchAttachmentsByTask(id));
    }
}
