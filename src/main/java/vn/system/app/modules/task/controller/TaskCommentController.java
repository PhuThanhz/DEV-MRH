package vn.system.app.modules.task.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.modules.task.domain.request.ReqCreateCommentDTO;
import vn.system.app.modules.task.domain.response.ResTaskCommentDTO;
import vn.system.app.modules.task.service.TaskCommentService;

@RestController
@RequestMapping("/api/v1")
public class TaskCommentController {

    private final TaskCommentService commentService;

    public TaskCommentController(TaskCommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/tasks/{id}/comments")
    @ApiMessage("Gửi bình luận thành công")
    public ResponseEntity<ResTaskCommentDTO> createComment(
            @PathVariable("id") Long id,
            @Valid @RequestBody ReqCreateCommentDTO req) {

        ResTaskCommentDTO dto = commentService.createComment(id, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/tasks/{id}/comments")
    @ApiMessage("Danh sách bình luận & nhật ký tác vụ")
    public ResponseEntity<List<ResTaskCommentDTO>> fetchComments(
            @PathVariable("id") Long id) {

        return ResponseEntity.ok(commentService.fetchCommentsByTask(id));
    }
}
