package vn.system.app.modules.task.domain;

import java.time.Instant;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.modules.task.domain.enums.TaskExtensionStatus;
import vn.system.app.modules.user.domain.User;

@Entity
@Table(name = "task_extension_requests", indexes = {
        @Index(name = "idx_ext_task_status", columnList = "task_id, status")
})
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class TaskExtensionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Task task;

    /**
     * Hạn chót của task tại thời điểm gửi yêu cầu — lưu lại để hiển thị lịch sử
     * "cũ -> mới" dù task.dueDate đã bị đổi bởi 1 yêu cầu khác sau đó.
     */
    private Instant currentDueDate;

    @NotNull(message = "Hạn chót đề xuất không được để trống")
    private Instant requestedDueDate;

    @NotBlank(message = "Lý do xin gia hạn không được để trống")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskExtensionStatus status = TaskExtensionStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by", nullable = false)
    @JsonIgnoreProperties({ "role", "userInfo", "directManager", "subordinates" })
    private User requestedBy;

    private Instant requestedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decided_by")
    @JsonIgnoreProperties({ "role", "userInfo", "directManager", "subordinates" })
    private User decidedBy;

    private Instant decidedAt;

    @Column(columnDefinition = "TEXT")
    private String decisionNote;

    @PrePersist
    public void beforeCreate() {
        this.requestedAt = Instant.now();
        if (this.status == null) {
            this.status = TaskExtensionStatus.PENDING;
        }
    }
}
