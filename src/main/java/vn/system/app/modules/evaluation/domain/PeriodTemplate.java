package vn.system.app.modules.evaluation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import vn.system.app.modules.evaluation.domain.enums.TemplateType;

/**
 * Bảng trung gian nối kỳ đánh giá với các template được dùng trong kỳ đó.
 * Một kỳ có thể dùng nhiều template:
 *   - Template F01 (MANAGER) cho cấp quản lý
 *   - Template F02 (STAFF) cho nhân viên thường
 */
@Entity
@Table(name = "period_templates",
        uniqueConstraints = @UniqueConstraint(columnNames = { "period_id", "template_id" }))
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PeriodTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id", nullable = false)
    @JsonIgnoreProperties("periodTemplates")
    private EvaluationPeriod period;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    @JsonIgnoreProperties("sections")
    private EvaluationTemplate template;

    @Enumerated(EnumType.STRING)
    @Column(name = "apply_to_role", length = 20)
    private TemplateType applyToRole;

}
