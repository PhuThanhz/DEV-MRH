package vn.system.app.modules.task.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.task.domain.request.ReqCreateChecklistDTO;
import vn.system.app.modules.task.domain.request.ReqUpdateChecklistDTO;
import vn.system.app.modules.task.domain.response.ResTaskChecklistDTO;
import vn.system.app.modules.task.service.TaskChecklistService;

@RestController
@RequestMapping("/api/v1")
public class TaskChecklistController {

    private final TaskChecklistService checklistService;

    public TaskChecklistController(TaskChecklistService checklistService) {
        this.checklistService = checklistService;
    }

    @PostMapping("/tasks/{id}/checklists")
    @ApiMessage("Thêm mục kiểm tra thành công")
    public ResponseEntity<ResTaskChecklistDTO> createItem(
            @PathVariable("id") Long id,
            @Valid @RequestBody ReqCreateChecklistDTO req) {

        ResTaskChecklistDTO dto = checklistService.createItem(id, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/tasks/{id}/checklists/{checklistId}")
    @ApiMessage("Cập nhật mục kiểm tra thành công")
    public ResponseEntity<ResTaskChecklistDTO> updateItem(
            @PathVariable("id") Long id,
            @PathVariable("checklistId") Long checklistId,
            @Valid @RequestBody ReqUpdateChecklistDTO req) {

        return ResponseEntity.ok(checklistService.updateItem(id, checklistId, req));
    }

    @PatchMapping("/tasks/{id}/checklists/{checklistId}/toggle")
    @ApiMessage("Cập nhật trạng thái hoàn thành mục kiểm tra")
    public ResponseEntity<ResTaskChecklistDTO> toggleItem(
            @PathVariable("id") Long id,
            @PathVariable("checklistId") Long checklistId,
            @RequestBody(required = false) Map<String, Object> body) {

        Boolean isCompleted = null;
        if (body != null && body.containsKey("isCompleted")) {
            Object raw = body.get("isCompleted");
            if (raw instanceof Boolean b) {
                isCompleted = b;
            } else if (raw != null) {
                isCompleted = Boolean.parseBoolean(raw.toString());
            }
        }

        return ResponseEntity.ok(checklistService.toggleItem(id, checklistId, isCompleted));
    }

    @DeleteMapping("/tasks/{id}/checklists/{checklistId}")
    @ApiMessage("Xóa mục kiểm tra thành công")
    public ResponseEntity<Void> deleteItem(
            @PathVariable("id") Long id,
            @PathVariable("checklistId") Long checklistId) {

        checklistService.deleteItem(id, checklistId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tasks/{id}/checklists")
    @ApiMessage("Danh sách mục kiểm tra tác vụ")
    public ResponseEntity<List<ResTaskChecklistDTO>> fetchChecklists(
            @PathVariable("id") Long id) {

        return ResponseEntity.ok(checklistService.fetchChecklistsByTask(id));
    }
}
