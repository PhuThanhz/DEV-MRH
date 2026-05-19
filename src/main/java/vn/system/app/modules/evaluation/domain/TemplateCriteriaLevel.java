package vn.system.app.modules.evaluation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Mô tả mức điểm của một tiêu chí.
 * Mỗi tiêu chí có 5 mức (level 1 → 5), mỗi mức có mô tả riêng.
 * Nhân viên / quản lý đọc mô tả để biết cần đạt gì để được mức đó.
 */
@Entity
@Table(name = "template_criteria_levels",
        uniqueConstraints = @UniqueConstraint(columnNames = { "criteria_id", "level" }))
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TemplateCriteriaLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criteria_id", nullable = false)
    @JsonIgnoreProperties("levels")
    private TemplateCriteria criteria;

    /**
     * Mức điểm: 1 (Chưa đạt) → 5 (Xuất sắc).
     * Constraint unique (criteria_id, level) đảm bảo mỗi mức chỉ có 1 mô tả.
     */
    @Column(nullable = false)
    private Integer level;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
}
