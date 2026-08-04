package vn.system.app.modules.task.domain;

import java.time.Instant;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.modules.task.domain.enums.TaskAttachmentCategory;
import vn.system.app.modules.user.domain.User;

@Entity
@Table(name = "task_attachments", indexes = {
        @Index(name = "idx_attachment_task_round", columnList = "task_id, submission_round"),
        @Index(name = "idx_attachment_task_category", columnList = "task_id, attachment_category")
})
@Getter
@Setter
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class TaskAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Task task;

    @NotBlank(message = "Tên file không được để trống")
    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, length = 50)
    private String folder = "documents";

    @Column(nullable = false)
    private Long fileSize = 0L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by", nullable = false)
    @JsonIgnoreProperties({ "role", "userInfo", "directManager", "subordinates" })
    private User uploadedBy;

    @Column(nullable = false)
    private boolean isResultAttachment = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "attachment_category", length = 20)
    private TaskAttachmentCategory attachmentCategory;

    @Column(nullable = false)
    private Integer submissionRound = 1;

    private Instant createdAt;

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
        if (this.folder == null) {
            this.folder = "documents";
        }
        if (this.fileSize == null) {
            this.fileSize = 0L;
        }
        if (this.submissionRound == null) {
            this.submissionRound = 1;
        }
        if (this.attachmentCategory == null) {
            this.attachmentCategory = this.isResultAttachment
                    ? TaskAttachmentCategory.RESULT
                    : TaskAttachmentCategory.WORKING;
        }
    }
}
