package vn.system.app.modules.evaluation.domain.response;

import lombok.Data;
import vn.system.app.modules.evaluation.domain.enums.NotificationType;
import java.time.Instant;

@Data
public class ResNotificationDTO {
    private Long id;
    private NotificationType notificationType;
    private String content;
    private String actionLink;
    private boolean read;
    private Instant createdAt;
}
