package vn.system.app.modules.notification.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import vn.system.app.modules.notification.event.AppNotificationEvent;
import vn.system.app.modules.notification.service.NotificationService;

@Component
public class NotificationHubListener {

    private final NotificationService notificationService;

    public NotificationHubListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAppNotificationEvent(AppNotificationEvent event) {
        if (event.getRecipientIds() == null) return;

        for (String userId : event.getRecipientIds()) {
            try {
                notificationService.sendNotification(
                    userId,
                    event.getModule(),
                    event.getType(),
                    event.getContent(),
                    event.getActionLink()
                );
            } catch (Exception e) {
                System.err.println("[Notification] Lỗi gửi cho user " + userId + ": " + e.getMessage());
            }
        }
    }
}
