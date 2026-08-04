package vn.system.app.modules.task.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.task.domain.request.ReqDecideExtensionDTO;
import vn.system.app.modules.task.domain.request.ReqRequestExtensionDTO;
import vn.system.app.modules.task.domain.response.ResTaskExtensionRequestDTO;
import vn.system.app.modules.task.service.TaskExtensionRequestService;

@RestController
@RequestMapping("/api/v1")
public class TaskExtensionRequestController {

    private final TaskExtensionRequestService extensionService;

    public TaskExtensionRequestController(TaskExtensionRequestService extensionService) {
        this.extensionService = extensionService;
    }

    @PostMapping("/tasks/{id}/extension-requests")
    @ApiMessage("Gửi yêu cầu gia hạn tác vụ thành công")
    public ResponseEntity<ResTaskExtensionRequestDTO> requestExtension(
            @PathVariable("id") Long id,
            @Valid @RequestBody ReqRequestExtensionDTO req) {

        ResTaskExtensionRequestDTO dto = extensionService.requestExtension(id, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/tasks/{id}/extension-requests/{extensionId}/decide")
    @ApiMessage("Xử lý yêu cầu gia hạn tác vụ thành công")
    public ResponseEntity<ResTaskExtensionRequestDTO> decideExtension(
            @PathVariable("id") Long id,
            @PathVariable("extensionId") Long extensionId,
            @Valid @RequestBody ReqDecideExtensionDTO req) {

        return ResponseEntity.ok(extensionService.decideExtension(id, extensionId, req));
    }
}
