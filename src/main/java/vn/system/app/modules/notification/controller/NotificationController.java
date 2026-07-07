package vn.system.app.modules.notification.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.system.app.common.response.ResultPaginationDTO;
import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.notification.domain.AppNotification;
import vn.system.app.modules.notification.domain.response.ResNotificationDTO;
import vn.system.app.modules.notification.service.NotificationService;
import vn.system.app.common.util.SecurityUtil;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @ApiMessage("Lấy danh sách thông báo")
    public ResponseEntity<List<ResNotificationDTO>> fetchMyNotifications() {
        String userId = getCurrentUserId();
        List<AppNotification> notifications = notificationService.fetchByUser(userId);
        
        List<ResNotificationDTO> res = notifications.stream()
                .map(notificationService::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/unread")
    @ApiMessage("Lấy danh sách thông báo chưa đọc")
    public ResponseEntity<List<ResNotificationDTO>> fetchMyUnreadNotifications() {
        String userId = getCurrentUserId();
        List<AppNotification> notifications = notificationService.fetchUnreadByUser(userId);
        
        List<ResNotificationDTO> res = notifications.stream()
                .map(notificationService::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/paginate")
    @ApiMessage("Lấy danh sách thông báo phân trang")
    public ResponseEntity<ResultPaginationDTO> fetchMyNotificationsPaginate(Pageable pageable) {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(notificationService.fetchPaginate(userId, pageable));
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
        AppNotification notification = notificationService.markAsRead(id, getCurrentUserId());
        return ResponseEntity.ok(notificationService.mapToDTO(notification));
    }

    @PatchMapping("/read-all")
    @ApiMessage("Đánh dấu tất cả thông báo đã đọc")
    public ResponseEntity<Void> markAllAsRead() {
        String userId = getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read-all/module")
    @ApiMessage("Đánh dấu tất cả thông báo theo module đã đọc")
    public ResponseEntity<Void> markAllAsReadByModule(@RequestParam String module) {
        String userId = getCurrentUserId();
        notificationService.markAllAsReadByModule(userId, module);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Xoá thông báo")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        String userId = getCurrentUserId();
        notificationService.deleteNotification(id, userId);
        return ResponseEntity.ok().build();
    }

    private String getCurrentUserId() {
        return SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new IdInvalidException("Chưa đăng nhập"));
    }
}
