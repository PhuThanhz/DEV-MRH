package vn.system.app.modules.careerpathtemplate.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import vn.system.app.modules.careerpath.domain.CareerPath;

@Entity
@Table(name = "career_path_template_steps", uniqueConstraints = @UniqueConstraint(columnNames = { "template_id",
        "step_order" }))
@Getter
@Setter
public class CareerPathTemplateStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private CareerPathTemplate template;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder; // 1, 2, 3...

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_path_id", nullable = false)
    private CareerPath careerPath; // chức danh ở bước này

    @Column(name = "duration_months")
    private Integer durationMonths; // dự kiến bao nhiêu tháng (null = đỉnh)

    @Column(columnDefinition = "TEXT")
    private String description; // mô tả thêm nếu cần
}