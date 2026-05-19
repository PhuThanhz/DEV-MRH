package vn.system.app.modules.evaluation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.modules.evaluation.domain.enums.TrainingGroup;

/**
 * Kế hoạch đào tạo năm sau do quản lý trực tiếp điền (Giai đoạn 2).
 *
 * Mỗi bản đánh giá có thể có nhiều kế hoạch đào tạo,
 * phân theo nhóm: kiến thức sản phẩm, chuyên môn, kỹ năng, ngoại ngữ.
 */
@Entity
@Table(name = "evaluation_training_plans")
@Getter
@Setter
public class EvaluationTrainingPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_record_id", nullable = false)
    @JsonIgnoreProperties({ "period", "employee", "directManager", "indirectManager", "template" })
    private EvaluationRecord evaluationRecord;

    @Enumerated(EnumType.STRING)
    @Column(name = "training_group", nullable = false, length = 30)
    private TrainingGroup trainingGroup;

    /** Nội dung cần đào tạo */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /** Yêu cầu cần đạt được sau đào tạo */
    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    /** Giải pháp / phương pháp đào tạo */
    @Column(name = "solution", columnDefinition = "TEXT")
    private String solution;

    /** Thời gian dự kiến hoàn thành đào tạo (mô tả tự do, ví dụ: "Q2/2026") */
    @Column(name = "completion_timeline", length = 100)
    private String completionTimeline;
}
