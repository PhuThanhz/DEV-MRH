package vn.system.app.modules.evaluation.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.modules.evaluation.domain.enums.CommentType;
import vn.system.app.modules.user.domain.User;

/**
 * Nhận xét văn bản trong quá trình đánh giá.
 *
 * Các loại nhận xét (CommentType):
 *  - SELF_REVIEW      : nhân viên tự nhận xét (Giai đoạn 1)
 *  - MANAGER_FEEDBACK : quản lý trực tiếp nhận xét (Giai đoạn 2)
 *  - REJECTION_REASON : lý do quản lý gián tiếp trả lại (Giai đoạn 3)
 */
@Entity
@Table(name = "evaluation_comments")
@Getter
@Setter
public class EvaluationComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_record_id", nullable = false)
    @JsonIgnoreProperties({ "period", "employee", "directManager", "indirectManager", "template" })
    private EvaluationRecord evaluationRecord;

    @Enumerated(EnumType.STRING)
    @Column(name = "comment_type", nullable = false, length = 30)
    private CommentType commentType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** Người viết nhận xét */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "written_by_user_id", nullable = false)
    @JsonIgnoreProperties({ "subordinates", "directManager", "userInfo", "role" })
    private User writtenBy;

    @Column(name = "written_at", nullable = false)
    private Instant writtenAt;

    @PrePersist
    public void beforeCreate() {
        this.writtenAt = Instant.now();
    }
}
