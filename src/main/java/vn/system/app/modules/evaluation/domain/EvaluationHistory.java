package vn.system.app.modules.evaluation.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.modules.evaluation.domain.enums.RecordStatus;
import vn.system.app.modules.user.domain.User;

/**
 * Lịch sử thay đổi trạng thái của một bản đánh giá.
 *
 * Mục đích:
 *  - Audit trail đầy đủ mọi thay đổi trạng thái.
 *  - Theo dõi số lần bị trả lại (đếm các hàng có fromStatus = PENDING_APPROVAL và toStatus = REVISION_NEEDED).
 *  - Lưu lý do trả lại để quản lý trực tiếp biết cần sửa gì.
 */
@Entity
@Table(name = "evaluation_history")
@Getter
@Setter
public class EvaluationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_record_id", nullable = false)
    @JsonIgnoreProperties({ "period", "employee", "directManager", "indirectManager", "template" })
    private EvaluationRecord evaluationRecord;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 40)
    private RecordStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 40)
    private RecordStatus toStatus;

    /** Người thực hiện thay đổi trạng thái */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_user_id", nullable = false)
    @JsonIgnoreProperties({ "subordinates", "directManager", "userInfo", "role" })
    private User performedBy;

    @Column(name = "performed_at", nullable = false)
    private Instant performedAt;

    /** Ghi chú bổ sung, ví dụ lý do trả lại khi toStatus = REVISION_NEEDED */
    @Column(columnDefinition = "TEXT")
    private String note;

    @PrePersist
    public void beforeCreate() {
        this.performedAt = Instant.now();
    }
}
