package vn.system.app.modules.task.domain;

import java.time.Instant;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.modules.task.domain.enums.TaskCommentType;
import vn.system.app.modules.user.domain.User;

@Entity
@Table(name = "task_comments", indexes = {
        @Index(name = "idx_comment_task_created", columnList = "task_id, created_at")
})
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class TaskComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({ "role", "userInfo", "directManager", "subordinates" })
    private User user;

    @NotBlank(message = "Nội dung bình luận không được để trống")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "TEXT")
    private String attachmentUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskCommentType type = TaskCommentType.COMMENT;

    private Integer relatedSubmissionRound;

    private Instant createdAt;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
        if (this.type == null) {
            this.type = TaskCommentType.COMMENT;
        }
    }
}
