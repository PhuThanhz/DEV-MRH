package vn.system.app.modules.jd.positionchart.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "jd_position_charts")
@Getter
@Setter
public class JDPositionChart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK sang job_descriptions
    @Column(nullable = false)
    private Long jobDescriptionId;

    // key dùng cho React Flow (node-1, node-2...)
    @Column(nullable = false)
    private String nodeKey;

    // Tên hiển thị
    @Column(nullable = false)
    private String title;

    // Node cha (null nếu root)
    private Long parentId;

    private Integer sortOrder;

    private Boolean active = true;
}