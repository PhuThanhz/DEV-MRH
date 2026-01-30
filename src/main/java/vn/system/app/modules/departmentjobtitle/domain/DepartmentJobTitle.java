package vn.system.app.modules.departmentjobtitle.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.jobtitle.domain.JobTitle;

@Entity
@Table(name = "department_job_titles")
@Getter
@Setter
public class DepartmentJobTitle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================
    // RELATION
    // =========================
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_title_id", nullable = false)
    private JobTitle jobTitle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    // =========================
    // STATUS
    // =========================
    // 1 = active, 0 = inactive (soft delete)
    @Column(nullable = false)
    private Integer status;

    // =========================
    // AUDIT
    // =========================
    @Column(updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;

    // =========================
    // LIFECYCLE
    // =========================
    @PrePersist
    protected void handleBeforeCreate() {
        Instant now = Instant.now();
        this.status = this.status == null ? 1 : this.status;
        this.createdAt = now;
        this.updatedAt = now;

        String user = SecurityUtil.getCurrentUserLogin().orElse("");
        this.createdBy = user;
        this.updatedBy = user;
    }

    @PreUpdate
    protected void handleBeforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
