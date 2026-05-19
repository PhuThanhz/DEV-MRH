package vn.system.app.modules.evaluation.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.modules.evaluation.domain.enums.NotificationType;
import vn.system.app.modules.user.domain.User;

/**
 * Thông báo gửi đến từng user trong hệ thống HQCV.
 *
 * Hệ thống tự động gửi thông báo:
 *  - PERIOD_OPENED         : khi kỳ được kích hoạt → gửi toàn bộ nhân viên
 *  - REMINDER_DEADLINE     : còn 3 ngày / 1 ngày → nhân viên chưa nộp
 *  - MANAGER_REVIEW_NEEDED : nhân viên nộp → gửi quản lý trực tiếp
 *  - APPROVAL_NEEDED       : quản lý trực tiếp gửi → gửi quản lý gián tiếp
 *  - REVISION_NEEDED       : quản lý gián tiếp trả lại → gửi quản lý trực tiếp
 *  - RESULT_AVAILABLE      : đã phê duyệt → gửi nhân viên
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
public class EvaluationNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Người nhận thông báo */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({ "subordinates", "directManager", "userInfo", "role" })
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 40)
    private NotificationType notificationType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Link dẫn trực tiếp đến form liên quan.
     * Ví dụ: /evaluation/records/123
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
