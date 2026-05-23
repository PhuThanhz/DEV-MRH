package vn.system.app.modules.notification.domain.response;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResNotificationDTO {
    private Long id;
    private String module;
    private String type;
    private String content;
    private String actionLink;
    private boolean read;
    private Instant createdAt;
}
