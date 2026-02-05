package vn.system.app.modules.companysalarygrade.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.system.app.common.util.SecurityUtil;

import java.time.Instant;

@Entity
@Table(name = "company_job_title_salary_grades")
@Getter
@Setter
public class CompanySalaryGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK → company_job_titles.id */
    @Column(name = "company_job_title_id", nullable = false)
    private Long companyJobTitleId;

    @Column(name = "grade_level", nullable = false)
    private Integer gradeLevel;

    @Column(nullable = false)
    private boolean active = true;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void beforeCreate() {
        this.active = true;
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}
