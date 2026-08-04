package vn.system.app.modules.task.domain;

import java.time.Instant;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.modules.user.domain.User;

@Entity
@Table(name = "task_submissions", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "task_id", "submission_round" })
}, indexes = {
        @Index(name = "idx_submission_task_round", columnList = "task_id, submission_round")
})
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class TaskSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Task task;

    @Column(nullable = false)
    private Integer submissionRound = 1;

    @NotBlank(message = "Báo cáo tóm tắt kết quả không được để trống")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String resultSummary;

    @Column(columnDefinition = "TEXT")
    private String deliverables;

    @Column(columnDefinition = "TEXT")
    private String issues;

    @Column(columnDefinition = "TEXT")
    private String nextSteps;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submitted_by", nullable = false)
    @JsonIgnoreProperties({ "role", "userInfo", "directManager", "subordinates" })
    private User submittedBy;

    private Instant submittedAt;

    @PrePersist
    public void beforeCreate() {
        this.submittedAt = Instant.now();
        if (this.submissionRound == null) {
            this.submissionRound = 1;
        }
    }
}
