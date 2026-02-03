// vn.system.app.modules.careerpath.domain.CareerPath

package vn.system.app.modules.careerpath.domain;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;
import vn.system.app.modules.department.domain.Department;
import vn.system.app.modules.jobtitle.domain.JobTitle;

@Entity
@Table(name = "career_paths")
@Getter
@Setter
public class CareerPath {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_title_id", nullable = false)
    private JobTitle jobTitle;

    @Column(columnDefinition = "TEXT")
    private String jobStandard;

    @Column(columnDefinition = "TEXT")
    private String trainingRequirement;

    @Column(columnDefinition = "TEXT")
    private String evaluationMethod;

    @Column(columnDefinition = "TEXT")
    private String requiredTime;

    @Column(columnDefinition = "TEXT")
    private String trainingOutcome;

    @Column(columnDefinition = "TEXT")
    private String performanceRequirement;

    @Column(columnDefinition = "TEXT")
    private String salaryNote;

    private Integer status;
    private boolean active = true;

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