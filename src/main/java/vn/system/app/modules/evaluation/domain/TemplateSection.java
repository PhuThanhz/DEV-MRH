package vn.system.app.modules.evaluation.domain;

import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Phần (Section) trong một template đánh giá.
 * Ví dụ: Phần A - Kết quả công việc, Phần B - Năng lực, Phần C - Thái độ.
 *
 * RULE: Tổng weight của tất cả section trong một template phải bằng 1.0 (100%).
 */
@Entity
@Table(name = "template_sections")
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TemplateSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    @JsonIgnoreProperties("sections")
    private EvaluationTemplate template;

    /** Mã code ngắn của phần, ví dụ: A, B, C */
    @Column(nullable = false, length = 10)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Trọng số của phần này trong tổng điểm.
     * Tổng weight của các section trong cùng template phải bằng 1.0.
     */
    @Column(nullable = false)
    private Double weight;

    /** Thứ tự hiển thị */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    // Các tiêu chí trong phần này
    @OneToMany(mappedBy = "section", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties("section")
    private List<TemplateCriteria> criteria;
}
