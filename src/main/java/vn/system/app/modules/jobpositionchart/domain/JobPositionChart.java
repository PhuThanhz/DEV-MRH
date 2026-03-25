package vn.system.app.modules.jobpositionchart.domain;

import java.time.Instant;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import vn.system.app.common.util.SecurityUtil;

@Entity
@Table(name = "job_position_charts")
@Getter
@Setter
public class JobPositionChart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // COMPANY / DEPARTMENT
    private String chartType;

    private Long companyId;

    private Long departmentId;

    private Boolean active = true;

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