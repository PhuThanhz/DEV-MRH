package vn.system.app.modules.notification.service;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.notification.domain.AppNotification;
import vn.system.app.modules.notification.domain.response.ResNotificationDTO;
import vn.system.app.modules.notification.repository.NotificationRepository;
import vn.system.app.modules.user.domain.User;
import vn.system.app.modules.user.repository.UserRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepo;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepo, UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.notificationRepo = notificationRepo;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Gửi thông báo dùng chung cho mọi module.
     * @param userId ID người nhận
     * @param module Module phát sinh (ví dụ: EVALUATION, JD_FLOW)
     * @param type Loại thông báo (ví dụ: MANAGER_REVIEW_NEEDED, APPROVAL_NEEDED)
     * @param content Nội dung hiển thị
     * @param actionLink Link điều hướng khi bấm vào
     * @return AppNotification
     */
    @Transactional
    public AppNotification sendNotification(String userId, String module, String type, String content, String actionLink) {
        User recipient = userRepository.findById(userId)
                .orElseThrow(() -> new IdInvalidException("Người dùng không tồn tại"));

        AppNotification notification = new AppNotification();
        notification.setRecipient(recipient);
        notification.setModule(module);
        notification.setType(type);
        notification.setContent(content);
        notification.setActionLink(actionLink);
        
        AppNotification saved = notificationRepo.save(notification);
        
        ResNotificationDTO dto = mapToDTO(saved);

        messagingTemplate.convertAndSendToUser(
            userId,
            "/queue/notifications",
            dto
        );
        
        return saved;
    }

    @Transactional
    public List<AppNotification> sendNotifications(Collection<String> userIds, String module, String type, String content, String actionLink) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<String> distinctIds = userIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (distinctIds.isEmpty()) {
            return List.of();
        }

        Map<String, User> usersById = userRepository.findAllById(distinctIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<AppNotification> notifications = distinctIds.stream()
                .map(usersById::get)
                .filter(user -> user != null)
                .map(user -> {
                    AppNotification notification = new AppNotification();
                    notification.setRecipient(user);
                    notification.setModule(module);
                    notification.setType(type);
                    notification.setContent(content);
                    notification.setActionLink(actionLink);
                    return notification;
                })
                .collect(Collectors.toList());

        List<AppNotification> saved = notificationRepo.saveAll(notifications);
        saved.forEach(notification -> messagingTemplate.convertAndSendToUser(
                notification.getRecipient().getId(),
                "/queue/notifications",
                mapToDTO(notification)));

        return saved;
    }

    public List<AppNotification> fetchByUser(String userId) {
        return notificationRepo.findTop50ByRecipientIdOrderByCreatedAtDesc(userId);
    }

    public List<AppNotification> fetchUnreadByUser(String userId) {
        return notificationRepo.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(userId);
    }

    public long countUnread(String userId) {
        return notificationRepo.countByRecipientIdAndReadFalse(userId);
    }

    @Transactional
    public AppNotification markAsRead(Long notificationId, String userId) {
        AppNotification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new IdInvalidException("Thông báo không tồn tại"));

        if (notification.getRecipient() == null || !notification.getRecipient().getId().equals(userId)) {
            throw new IdInvalidException("Bạn không có quyền cập nhật thông báo này");
        }

        notification.setRead(true);
        return notificationRepo.save(notification);
    }

    @Transactional
    public void markAllAsRead(String userId) {
        notificationRepo.markAllAsReadByRecipientId(userId);
    }
    
    public ResNotificationDTO mapToDTO(AppNotification entity) {
        ResNotificationDTO dto = new ResNotificationDTO();
        dto.setId(entity.getId());
        dto.setModule(entity.getModule());
        dto.setType(entity.getType());
        dto.setContent(entity.getContent());
        dto.setActionLink(entity.getActionLink());
        dto.setRead(entity.isRead());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
