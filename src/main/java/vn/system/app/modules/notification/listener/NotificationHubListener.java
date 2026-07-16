package vn.system.app.modules.notification.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import vn.system.app.modules.notification.event.AppNotificationEvent;
import vn.system.app.modules.notification.service.NotificationService;
import vn.system.app.modules.user.repository.UserRepository;
import vn.system.app.modules.email.service.EmailService;

@Component
public class NotificationHubListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationHubListener.class);

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public NotificationHubListener(
            NotificationService notificationService,
            UserRepository userRepository,
            EmailService emailService) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("taskExecutor")
    public void handleAppNotificationEvent(AppNotificationEvent event) {
        if (event.getRecipientIds() == null) return;

        try {
            notificationService.sendNotifications(
                event.getRecipientIds(),
                event.getModule(),
                event.getType(),
                event.getContent(),
                event.getActionLink()
            );
        } catch (Exception e) {
            log.error("[Notification] Lỗi gửi thông báo hàng loạt", e);
        }

        // 3.7: Tự động gửi email bất đồng bộ cho các mốc quan trọng của phân hệ Đánh giá
        if ("EVALUATION".equals(event.getModule())) {
            String type = event.getType();
            if ("PERIOD_OPENED".equals(type) || "MANAGER_REVIEW_NEEDED".equals(type) ||
                "APPROVAL_NEEDED".equals(type) || "RESULT_AVAILABLE".equals(type) ||
                "REVISION_NEEDED".equals(type) || "MANAGER_ASSIGNED".equals(type) ||
                "APPROVER_ASSIGNED".equals(type) || "REMINDER_DEADLINE".equals(type) ||
                "DEADLINE_EXTENDED".equals(type) || "REMIND_ACKNOWLEDGE".equals(type) ||
                "ESCALATION".equals(type) ||
                (type != null && type.contains("WARNING"))) {
                
                try {
                    for (String recipientId : event.getRecipientIds()) {
                        userRepository.findById(recipientId).ifPresent(user -> {
                            if (user.getEmail() != null && !user.getEmail().isBlank()) {
                                String subject = "[Lotus HRM] Thông báo phân hệ đánh giá HQCV";
                                if ("PERIOD_OPENED".equals(type)) {
                                    subject = "[Lotus HRM] Mở kỳ đánh giá HQCV mới";
                                } else if ("MANAGER_REVIEW_NEEDED".equals(type)) {
                                    subject = "[Lotus HRM] Có bản đánh giá cần quản lý chấm điểm";
                                } else if ("APPROVAL_NEEDED".equals(type)) {
                                    subject = "[Lotus HRM] Có bản đánh giá cần phê duyệt";
                                } else if ("RESULT_AVAILABLE".equals(type)) {
                                    subject = "[Lotus HRM] Đã có kết quả đánh giá HQCV";
                                } else if ("REVISION_NEEDED".equals(type)) {
                                    subject = "[Lotus HRM] Yêu cầu sửa đổi/bổ sung đánh giá HQCV";
                                } else if ("DEADLINE_EXTENDED".equals(type)) {
                                    subject = "[Lotus HRM] Deadline đánh giá HQCV đã được gia hạn";
                                } else if ("REMINDER_DEADLINE".equals(type)) {
                                    subject = "[Lotus HRM] Nhắc nhở hạn chót đánh giá HQCV";
                                } else if ("REMIND_ACKNOWLEDGE".equals(type)) {
                                    subject = "[Lotus HRM] Nhắc xác nhận kết quả đánh giá HQCV";
                                } else if ("ESCALATION".equals(type)) {
                                    subject = "[Lotus HRM] Cảnh báo quá hạn đánh giá HQCV (Escalation)";
                                }
                                
                                String mailContent = String.format(
                                    "Chào %s,\n\nBạn có thông báo mới từ phân hệ Đánh giá HQCV:\n\n\"%s\"\n\nVui lòng truy cập hệ thống để xử lý.\n\nTrân trọng,\nLotus HRM Support Team",
                                    user.getName(), event.getContent()
                                );
                                emailService.sendEmailAsync(user.getEmail(), subject, mailContent, false);
                            }
                        });
                    }
                } catch (Exception e) {
                    log.error("[Email] Lỗi gửi email cho thông báo Đánh giá", e);
                }
            }
        }
    }
}
