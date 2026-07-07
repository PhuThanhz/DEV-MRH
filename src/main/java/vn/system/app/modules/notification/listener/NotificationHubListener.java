package vn.system.app.modules.notification.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import vn.system.app.modules.notification.event.AppNotificationEvent;
import vn.system.app.modules.notification.service.NotificationService;

@Component
public class NotificationHubListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationHubListener.class);

    private final NotificationService notificationService;

    public NotificationHubListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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
    }
}
