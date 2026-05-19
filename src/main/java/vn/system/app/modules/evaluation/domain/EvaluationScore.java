package vn.system.app.modules.evaluation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.modules.evaluation.domain.enums.ScoredBy;

/**
 * Điểm chấm cho từng tiêu chí trong một bản đánh giá.
 *
 * Mỗi record + criteria sẽ có 2 hàng:
 *   - scored_by = EMPLOYEE  (nhân viên tự chấm)
 *   - scored_by = MANAGER   (quản lý trực tiếp chấm)
 *
 * RULE: Tiêu chí cha (parentCriteria != null trong TemplateCriteria):
 *   - score = trung bình cộng score của các sub-tiêu chí con
 *   - không cho nhập tay
 *
 * RULE: weightedScore = score × criteria.weight × section.weight
 */
@Entity
@Table(name = "evaluation_scores",
        uniqueConstraints = @UniqueConstraint(columnNames = { "evaluation_record_id", "criteria_id", "scored_by" }))
@Getter
@Setter
public class EvaluationScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_record_id", nullable = false)
    @JsonIgnoreProperties({ "period", "employee", "directManager", "indirectManager", "template" })
    private EvaluationRecord evaluationRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criteria_id", nullable = false)
    @JsonIgnoreProperties({ "section", "subCriteria", "parentCriteria", "levels" })
    private TemplateCriteria criteria;

    /** EMPLOYEE hoặc MANAGER */
    @Enumerated(EnumType.STRING)
    @Column(name = "scored_by", nullable = false, length = 20)
    private ScoredBy scoredBy;

    /**
     * Điểm thô từ 1 đến 5.
     * Với tiêu chí cha, giá trị này được tính tự động = trung bình sub.
     */
    @Column(nullable = false)
    private Double score;

    /**
     * Điểm có trọng số = score × criteria.weight × section.weight.
     * Được tính và lưu mỗi khi nhập/sửa score.
     */
    @Column(name = "weighted_score", nullable = false)
    private Double weightedScore;
}
