package vn.system.app.modules.evaluation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.evaluation.domain.EvaluationNotification;
import vn.system.app.modules.evaluation.domain.response.ResNotificationDTO;
import vn.system.app.modules.evaluation.service.EvaluationNotificationService;
import vn.system.app.common.util.SecurityUtil;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/evaluation/notifications")
public class EvaluationNotificationController {

    private final EvaluationNotificationService notificationService;

    public EvaluationNotificationController(EvaluationNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @ApiMessage("Lấy danh sách thông báo")
    public ResponseEntity<List<ResNotificationDTO>> fetchMyNotifications() {
        String userId = getCurrentUserId();
        List<EvaluationNotification> notifications = notificationService.fetchByUser(userId);
        
        List<ResNotificationDTO> res = notifications.stream().map(this::mapToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/unread")
    @ApiMessage("Lấy danh sách thông báo chưa đọc")
    public ResponseEntity<List<ResNotificationDTO>> fetchMyUnreadNotifications() {
        String userId = getCurrentUserId();
        List<EvaluationNotification> notifications = notificationService.fetchUnreadByUser(userId);
        
        List<ResNotificationDTO> res = notifications.stream().map(this::mapToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/unread/count")
    @ApiMessage("Đếm số thông báo chưa đọc")
    public ResponseEntity<Long> countUnread() {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(notificationService.countUnread(userId));
    }

    @PatchMapping("/{id}/read")
    @ApiMessage("Đánh dấu 1 thông báo đã đọc")
    public ResponseEntity<ResNotificationDTO> markAsRead(@PathVariable Long id) {
        EvaluationNotification notification = notificationService.markAsRead(id, getCurrentUserId());
        return ResponseEntity.ok(mapToDTO(notification));
    }

    @PatchMapping("/read-all")
    @ApiMessage("Đánh dấu tất cả thông báo đã đọc")
    public ResponseEntity<Void> markAllAsRead() {
        String userId = getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    private String getCurrentUserId() {
        return SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new IdInvalidException("Chưa đăng nhập"));
    }

    private ResNotificationDTO mapToDTO(EvaluationNotification entity) {
        ResNotificationDTO dto = new ResNotificationDTO();
        dto.setId(entity.getId());
        dto.setNotificationType(entity.getNotificationType());
        dto.setContent(entity.getContent());
        dto.setActionLink(entity.getActionLink());
        dto.setRead(entity.isRead());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
