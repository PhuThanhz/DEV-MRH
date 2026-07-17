package vn.system.app.modules.notification.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.modules.user.domain.User;

/**
 * Thông báo dùng chung cho toàn hệ thống (Generic Notification).
 */
@Entity
@Table(name = "notifications",
        indexes = {
            @Index(name = "idx_notif_recipient_type_link_created", columnList = "user_id, notification_type, action_link, created_at"),
            @Index(name = "idx_notif_user_created", columnList = "user_id, created_at"),
            @Index(name = "idx_notif_user_read_created", columnList = "user_id, is_read, created_at")
        })
@Getter
@Setter
public class AppNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Người nhận thông báo */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({ "subordinates", "directManager", "userInfo", "role" })
    private User recipient;

    /** Module sinh ra thông báo (Ví dụ: EVALUATION, JD_FLOW, PROCEDURES) */
    @Column(name = "module", length = 50)
    private String module;

    /** Loại thông báo, chuyển thành String để mở rộng cho các module khác nhau */
    @Column(name = "notification_type", nullable = false, length = 40)
    private String type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Link dẫn trực tiếp đến form liên quan.
     */
    @Column(name = "action_link", length = 500)
    private String actionLink;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
    }
}
