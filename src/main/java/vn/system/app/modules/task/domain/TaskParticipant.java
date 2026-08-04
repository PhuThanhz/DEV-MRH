package vn.system.app.modules.task.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.modules.task.domain.enums.TaskParticipantRole;
import vn.system.app.modules.user.domain.User;

@Entity
@Table(name = "task_participants", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "task_id", "user_id", "role" })
}, indexes = {
        @Index(name = "idx_participant_user_role", columnList = "user_id, role")
})
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class TaskParticipant {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskParticipantRole role;

    private Instant createdAt;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
    }
}
