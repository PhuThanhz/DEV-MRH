package vn.system.app.modules.evaluation.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.system.app.common.util.error.IdInvalidException;
import vn.system.app.modules.evaluation.domain.EvaluationNotification;
import vn.system.app.modules.evaluation.repository.EvaluationNotificationRepository;

/**
 * Service quản lý thông báo đánh giá HQCV.
 */
@Service
public class EvaluationNotificationService {

    private final EvaluationNotificationRepository notificationRepo;

    public EvaluationNotificationService(EvaluationNotificationRepository notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    /** Lấy tất cả thông báo của user, mới nhất trước */
    public List<EvaluationNotification> fetchByUser(String userId) {
        return notificationRepo.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    /** Lấy thông báo chưa đọc */
    public List<EvaluationNotification> fetchUnreadByUser(String userId) {
        return notificationRepo.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(userId);
    }

    /** Đếm thông báo chưa đọc (cho badge) */
    public long countUnread(String userId) {
        return notificationRepo.countByRecipientIdAndReadFalse(userId);
    }

    /** Đánh dấu đã đọc */
    @Transactional
    public EvaluationNotification markAsRead(Long notificationId, String userId) {
        EvaluationNotification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new IdInvalidException("Thông báo không tồn tại"));

        if (notification.getRecipient() == null || !notification.getRecipient().getId().equals(userId)) {
            throw new IdInvalidException("Bạn không có quyền cập nhật thông báo này");
        }

        notification.setRead(true);
        return notificationRepo.save(notification);
    }

    /** Đánh dấu tất cả đã đọc */
    @Transactional
    public void markAllAsRead(String userId) {
        List<EvaluationNotification> unread = notificationRepo
                .findByRecipientIdAndReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepo.saveAll(unread);
    }
}
