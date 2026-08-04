package vn.system.app.modules.task.domain;

import java.time.Instant;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.modules.user.domain.User;

@Entity
@Table(name = "task_checklists", indexes = {
        @Index(name = "idx_checklist_task_sort", columnList = "task_id, sort_order")
})
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class TaskChecklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Task task;

    @NotBlank(message = "Tiêu đề mục kiểm tra không được để trống")
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private boolean isCompleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    @JsonIgnoreProperties({ "role", "userInfo", "directManager", "subordinates" })
    private User assignedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by")
    @JsonIgnoreProperties({ "role", "userInfo", "directManager", "subordinates" })
    private User completedBy;

    private Instant completedAt;

    @Column(nullable = false)
    private Integer sortOrder = 0;
}
