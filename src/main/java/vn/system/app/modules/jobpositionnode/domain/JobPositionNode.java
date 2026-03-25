package vn.system.app.modules.jobpositionnode.domain;

import java.time.Instant;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.jobpositionchart.domain.JobPositionChart;
import vn.system.app.modules.jd.jobdescription.domain.JobDescription;

@Entity
@Table(name = "job_position_nodes")
@Getter
@Setter
public class JobPositionNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // mã cấp bậc
    private String level;

    // tên người giữ chức danh
    private String holderName;

    // node mục tiêu
    private Boolean isGoal = false;

    private Long parentId;

    private Integer sortOrder;

    private Boolean active = true;

    // ── Vị trí trên canvas ──────────────────
    private Double posX;

    private Double posY;

    /*
     * ==========================
     * CHART RELATION (N-1)
     * ==========================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chart_id")
    private JobPositionChart chart;

    /*
     * ==========================
     * JD RELATION (N-1)
     * ==========================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_description_id")
    private JobDescription jobDescription;

    private Instant createdAt;
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;

    /*
     * ==========================
     * AUDIT
     * ==========================
     */

    @PrePersist
    public void beforeCreate() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}