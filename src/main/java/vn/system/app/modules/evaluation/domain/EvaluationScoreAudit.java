package vn.system.app.modules.evaluation.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.modules.evaluation.domain.enums.ScoredBy;
import vn.system.app.modules.user.domain.User;

/**
 * Lịch sử thay đổi điểm số của tiêu chí (Audit trail).
 */
@Entity
@Table(name = "evaluation_score_audit",
        indexes = {
            @Index(name = "idx_eval_score_audit_record_id", columnList = "evaluation_record_id")
        })
@Getter
@Setter
public class EvaluationScoreAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_record_id", nullable = false)
    @JsonIgnoreProperties({ "period", "employee", "directManager", "indirectManager", "template" })
    private EvaluationRecord evaluationRecord;

    @Column(name = "criteria_id", nullable = false)
    private Long criteriaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scored_by", nullable = false, length = 20)
    private ScoredBy scoredBy;

    @Column(name = "old_score")
    private Double oldScore;

    @Column(name = "new_score", nullable = false)
    private Double newScore;

    /** Người thực hiện thay đổi */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id")
    @JsonIgnoreProperties({ "subordinates", "directManager", "userInfo", "role" })
    private User changedBy;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @PrePersist
    public void beforeCreate() {
        this.changedAt = Instant.now();
    }
}
