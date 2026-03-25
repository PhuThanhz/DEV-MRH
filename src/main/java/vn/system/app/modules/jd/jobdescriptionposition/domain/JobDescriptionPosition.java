package vn.system.app.modules.jd.jobdescriptionposition.domain;

import java.time.Instant;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.jd.jobdescription.domain.JobDescription;
import vn.system.app.modules.jobpositionchart.domain.JobPositionChart;
import vn.system.app.modules.jobpositionnode.domain.JobPositionNode;

@Entity
@Table(name = "job_description_positions")
@Getter
@Setter
public class JobDescriptionPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * =========================
     * RELATION
     * =========================
     */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_description_id")
    private JobDescription jobDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chart_id")
    private JobPositionChart chart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id")
    private JobPositionNode node;

    /*
     * =========================
     * NODE INFO (CACHE)
     * =========================
     */

    private String nodeName;

    private String levelCode;

    /*
     * =========================
     * AUDIT
     * =========================
     */

    private Instant createdAt;
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;

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