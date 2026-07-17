package vn.system.app.modules.evaluation.domain;

import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Tiêu chí đánh giá trong một section.
 *
 * Hỗ trợ cấu trúc cha-con (parent_id):
 *  - Nếu parentCriteria = null → đây là tiêu chí cha.
 *  - Nếu parentCriteria != null → đây là sub-tiêu chí con.
 *
 * RULE: Điểm tiêu chí cha = trung bình cộng điểm các sub-tiêu chí (không cho nhập tay).
 * RULE: Trọng số tiêu chí cha là trọng số tuyệt đối trên toàn mẫu; tổng trong
 * cùng section phải bằng trọng số của section.
 */
@Entity
@Table(name = "template_criteria")
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TemplateCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    @JsonIgnoreProperties("criteria")
    private TemplateSection section;

    @Column(nullable = false, length = 300)
    private String name;

    /** Phương pháp đo lường / mô tả cách tính điểm */
    @Column(name = "measurement_method", columnDefinition = "TEXT")
    private String measurementMethod;

    /** Nội dung chi tiết của tiêu chí / sub-tiêu chí */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Trọng số tuyệt đối của tiêu chí này trong toàn mẫu.
     * Chỉ áp dụng cho tiêu chí cha (parentCriteria = null).
     * Tổng weight các tiêu chí cha phải bằng weight của section; tiêu chí con = 0.
     */
    @Column(nullable = false)
    private Double weight;

    /** Thứ tự hiển thị trong section */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    /** Tiêu chí cha. Nếu null thì đây là tiêu chí gốc */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties({ "subCriteria", "section" })
    private TemplateCriteria parentCriteria;

    /** Danh sách sub-tiêu chí con */
    @OneToMany(mappedBy = "parentCriteria", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties({ "parentCriteria", "section" })
    private List<TemplateCriteria> subCriteria;

    /** Mô tả các mức điểm 1-5 của tiêu chí này */
    @OneToMany(mappedBy = "criteria", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnoreProperties("criteria")
    private List<TemplateCriteriaLevel> levels;
}
